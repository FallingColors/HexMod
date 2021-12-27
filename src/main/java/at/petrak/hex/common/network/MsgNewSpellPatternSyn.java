package at.petrak.hex.common.network;

import at.petrak.hex.common.casting.CastingContext;
import at.petrak.hex.common.casting.CastingHarness;
import at.petrak.hex.common.casting.CastingHarness.CastResult;
import at.petrak.hex.common.items.ItemWand;
import at.petrak.hex.hexmath.HexPattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a MsgNewSpellPatternAck packet
 */
public record MsgNewSpellPatternSyn(InteractionHand handUsed, HexPattern pattern) {
    public static MsgNewSpellPatternSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = InteractionHand.values()[buf.readInt()];
        var pattern = HexPattern.DeserializeFromNBT(buf.readAnySizeNbt());
        return new MsgNewSpellPatternSyn(hand, pattern);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.handUsed.ordinal());
        buf.writeNbt(this.pattern.serializeToNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                var held = sender.getItemInHand(this.handUsed);
                if (held.getItem() instanceof ItemWand) {
                    var tag = held.getOrCreateTag();
                    var harness = CastingHarness.DeserializeFromNBT(tag, sender);

                    var res = harness.update(this.pattern);
                    if (res instanceof CastResult.Success success) {
                        success.getSpell().cast(new CastingContext(sender));
                    } else if (res instanceof CastResult.Error error) {
                        sender.sendMessage(new TextComponent(error.getExn().toString()), Util.NIL_UUID);
                    }

                    boolean quit;
                    if (res instanceof CastResult.Nothing) {
                        // save the changes
                        held.setTag(harness.serializeToNBT());
                        quit = false;
                    } else {
                        // Else we requested to quit in some way or another
                        held.setTag(new CompoundTag());
                        quit = true;
                    }
                    HexMessages.getNetwork()
                            .send(PacketDistributor.PLAYER.with(() -> sender),
                                    new MsgNewSpellPatternAck(quit));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
