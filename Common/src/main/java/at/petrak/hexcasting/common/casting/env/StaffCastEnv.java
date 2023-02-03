package at.petrak.hexcasting.common.casting.env;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.ControllerInfo;
import at.petrak.hexcasting.api.casting.eval.ResolvedPattern;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexCoord;
import at.petrak.hexcasting.api.misc.FrozenColorizer;
import at.petrak.hexcasting.api.mod.HexStatistics;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.common.network.MsgNewSpellPatternAck;
import at.petrak.hexcasting.common.network.MsgNewSpellPatternSyn;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.HashSet;
import java.util.List;

public class StaffCastEnv extends PlayerBasedCastEnv {
    public StaffCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);
    }

    @Override
    public void postExecution(CastResult result) {
        for (var sideEffect : result.getSideEffects()) {
            if (sideEffect instanceof OperatorSideEffect.DoMishap doMishap) {
                this.sendMishapMsgToPlayer(doMishap);
            }
        }
    }

    @Override
    public long extractMedia(long cost) {
        var canOvercast = this.canOvercast();
        var remaining = this.extractMediaFromInventory(cost, canOvercast);
        if (remaining > 0 && !canOvercast) {
            this.caster.sendSystemMessage(Component.translatable("hexcasting.message.cant_overcast"));
        }
        return remaining;
    }

    @Override
    public FrozenColorizer getColorizer() {
        return HexAPI.instance().getColorizer(this.caster);
    }

    public static void handleNewPatternOnServer(ServerPlayer sender, MsgNewSpellPatternSyn msg) {
        var held = sender.getItemInHand(msg.handUsed());
        if (held.is(HexTags.Items.STAVES)) {
            boolean autoFail = false;

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
                    autoFail = true;
                }
            }

            sender.awardStat(HexStatistics.PATTERNS_DRAWN);

            var harness = IXplatAbstractions.INSTANCE.getStaffHarness(sender, msg.handUsed());

            ControllerInfo clientInfo;
            if (autoFail) {
                var descs = harness.generateDescs();
                clientInfo = new ControllerInfo(harness.getStack().isEmpty(), ResolvedPatternType.INVALID,
                    descs.getFirst(), descs.getSecond(), descs.getThird(), harness.getParenCount());
            } else {
                clientInfo = harness.executeIota(new PatternIota(msg.pattern()), sender.getLevel());
            }

            if (clientInfo.isStackClear()) {
                IXplatAbstractions.INSTANCE.setHarness(sender, null);
                IXplatAbstractions.INSTANCE.setPatterns(sender, List.of());
            } else {
                IXplatAbstractions.INSTANCE.setHarness(sender, harness);
                if (!resolvedPatterns.isEmpty()) {
                    resolvedPatterns.get(resolvedPatterns.size() - 1).setType(clientInfo.getResolutionType());
                }
                IXplatAbstractions.INSTANCE.setPatterns(sender, resolvedPatterns);
            }

            IXplatAbstractions.INSTANCE.sendPacketToPlayer(sender,
                new MsgNewSpellPatternAck(clientInfo, resolvedPatterns.size() - 1));
        }

    }
}
