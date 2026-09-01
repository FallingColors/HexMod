package at.petrak.hexcasting.common.msgs;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.env.StaffCastEnv;
import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import at.petrak.hexcasting.common.effects.DissociationEffect;
import at.petrak.hexcasting.common.lib.HexMobEffects;
import at.petrak.hexcasting.common.misc.HexMobEffect;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector3f;

/**
 * Sent client->server when the player pans the casting grid.
 */
public record MsgPannedGridC2S(Vec2 panOffset) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MsgPannedGridC2S> TYPE = new CustomPacketPayload.Type<>(HexAPI.modLoc("pan_cs"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MsgPannedGridC2S> STREAM_CODEC = StreamCodec.composite(
            HexUtils.VEC2_STREAM_CODEC, MsgPannedGridC2S::panOffset,
            MsgPannedGridC2S::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(MinecraftServer server, ServerPlayer sender) {
        server.execute(() -> {
            IXplatAbstractions.INSTANCE.setPanOffset(sender, panOffset);
            var inst = sender.getEffect(HexMobEffects.DISSOCIATION);
            int currentDur = (inst != null) ? inst.getDuration() : 0;
            if (panOffset.lengthSquared() > 600*600) {
                int newDur = Math.min(currentDur + 80, DissociationEffect.AMP_1_DURATION);
                sender.addEffect(new MobEffectInstance(HexMobEffects.DISSOCIATION, newDur, 1, false, false, true));
            } else if (panOffset.lengthSquared() > 300*300) {
                int newDur = Math.min(currentDur + 40, DissociationEffect.AMP_0_DURATION);
                sender.addEffect(new MobEffectInstance(HexMobEffects.DISSOCIATION, newDur, 0, false, false, true));
            }
        });
    }
}
