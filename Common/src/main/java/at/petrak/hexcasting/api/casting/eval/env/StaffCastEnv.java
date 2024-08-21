package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ParticleSpray;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.mod.HexStatistics;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.common.msgs.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StaffCastEnv extends PlayerBasedCastEnv {
    private final InteractionHand castingHand;

    private final Set<HexPattern> castPatterns = new HashSet<>();
    private int soundsPlayed = 0;


    public StaffCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);

        this.castingHand = castingHand;
    }

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);

        if (result.component1() instanceof PatternIota patternIota) {
            castPatterns.add(patternIota.getPattern());
        }

        // we always want to play this sound one at a time
        var sound = result.getSound().sound();
        if (soundsPlayed < 100 && sound != null) { // TODO: Make configurable
            var soundPos = this.caster.position();
            this.world.playSound(null, soundPos.x, soundPos.y, soundPos.z,
                sound, SoundSource.PLAYERS, 1f, 1f);
            soundsPlayed++;
        }
    }

    @Override
    public void postCast(CastingImage image) {
        super.postCast(image);

        var packet = new MsgNewSpiralPatternsS2C(
            this.caster.getUUID(), castPatterns.stream().toList(), Integer.MAX_VALUE
        );
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(this.caster, packet);
        IXplatAbstractions.INSTANCE.sendPacketTracking(this.caster, packet);

        castPatterns.clear();
        soundsPlayed = 0;
    }

    @Override
    public long extractMediaEnvironment(long cost) {
        if (this.caster.isCreative())
            return 0;

        var canOvercast = this.canOvercast();
        var remaining = this.extractMediaFromInventory(cost, canOvercast);
        if (remaining > 0 && !canOvercast) {
            this.caster.sendSystemMessage(Component.translatable("hexcasting.message.cant_overcast"));
        }
        return remaining;
    }

    @Override
    public InteractionHand getCastingHand() {
        return castingHand;
    }

    @Override
    public FrozenPigment getPigment() {
        return HexAPI.instance().getColorizer(this.caster);
    }

    public static void handleNewPatternOnServer(ServerPlayer sender, MsgNewSpellPatternC2S msg) {
        boolean cheatedPatternOverlap = false;

        List<ResolvedPattern> resolvedPatterns = msg.resolvedPatterns();
        if (!resolvedPatterns.isEmpty()) {
            var allPoints = new HashSet<HexCoord>();
            for (int i = 0; i < resolvedPatterns.size() - 1; i++) {
                ResolvedPattern pat = resolvedPatterns.get(i);
                allPoints.addAll(pat.getPattern().positions(pat.getOrigin()));
            }
            var currentResolvedPattern = resolvedPatterns.get(resolvedPatterns.size() - 1);
            var currentSpellPoints = currentResolvedPattern.getPattern()
                .positions(currentResolvedPattern.getOrigin());
            if (currentSpellPoints.stream().anyMatch(allPoints::contains)) {
                cheatedPatternOverlap = true;
            }
        }

        if (cheatedPatternOverlap) {
            return;
        }

        sender.awardStat(HexStatistics.PATTERNS_DRAWN);

        var vm = IXplatAbstractions.INSTANCE.getStaffcastVM(sender, msg.handUsed());

        // TODO: do we reset the number of evals run via the staff? because each new pat is a new tick.

        ExecutionClientView clientInfo = vm.queueExecuteAndWrapIota(new PatternIota(msg.pattern()), sender.serverLevel());

        if (clientInfo.isStackClear()) {
            IXplatAbstractions.INSTANCE.setStaffcastImage(sender, null);
            IXplatAbstractions.INSTANCE.setPatterns(sender, List.of());
        } else {
            IXplatAbstractions.INSTANCE.setStaffcastImage(sender, vm.getImage().withOverriddenUsedOps(0));
            if (!resolvedPatterns.isEmpty()) {
                resolvedPatterns.get(resolvedPatterns.size() - 1).setType(clientInfo.getResolutionType());
            }
            IXplatAbstractions.INSTANCE.setPatterns(sender, resolvedPatterns);
        }

        IXplatAbstractions.INSTANCE.sendPacketToPlayer(sender,
            new MsgNewSpellPatternS2C(clientInfo, resolvedPatterns.size() - 1));

        IMessage packet;
        if (clientInfo.isStackClear()) {
            packet = new MsgClearSpiralPatternsS2C(sender.getUUID());
        } else {
            packet = new MsgNewSpiralPatternsS2C(sender.getUUID(), List.of(msg.pattern()), Integer.MAX_VALUE);
        }
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(sender, packet);
        IXplatAbstractions.INSTANCE.sendPacketTracking(sender, packet);

        if (clientInfo.getResolutionType().getSuccess()) {
            // Somehow we lost spraying particles on each new pattern, so do it here
            // this also nicely prevents particle spam on trinkets
            new ParticleSpray(sender.position(), new Vec3(0.0, 1.5, 0.0), 0.4, Math.PI / 3, 30)
                .sprayParticles(sender.serverLevel(), IXplatAbstractions.INSTANCE.getPigment(sender));
        }
    }
}
