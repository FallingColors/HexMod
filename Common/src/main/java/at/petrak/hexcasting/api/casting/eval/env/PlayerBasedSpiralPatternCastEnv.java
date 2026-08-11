package at.petrak.hexcasting.api.casting.eval.env;

import at.petrak.hexcasting.api.casting.eval.CastResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.msgs.MsgNewSpiralPatternsS2C;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.HashSet;
import java.util.Set;

/**
 * Variant of {@link PlayerBasedCastEnv} that tracks executed patterns and displays them in a spiral around the player
 */
public abstract class PlayerBasedSpiralPatternCastEnv extends PlayerBasedCastEnv {
    private final Set<HexPattern> castPatterns = new HashSet<>();

    public PlayerBasedSpiralPatternCastEnv(ServerPlayer caster, InteractionHand castingHand) {
        super(caster, castingHand);
    }

    /**
     * Returns the duration (in ticks) that patterns should remain visible after being executed by the env.
     */
    public abstract int getSpiralPatternDuration();

    @Override
    public void postExecution(CastResult result) {
        super.postExecution(result);

        if (result.component1() instanceof PatternIota patternIota) {
            castPatterns.add(patternIota.getPattern());
        }
    }

    @Override
    public void postCast(CastingImage image) {
        super.postCast(image);

        var packet = new MsgNewSpiralPatternsS2C(
                this.caster.getUUID(), castPatterns.stream().toList(), getSpiralPatternDuration()
        );
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(this.caster, packet);
        IXplatAbstractions.INSTANCE.sendPacketTracking(this.caster, packet);

        castPatterns.clear();
    }
}
