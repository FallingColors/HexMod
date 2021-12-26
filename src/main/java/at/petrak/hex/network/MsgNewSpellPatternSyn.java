package at.petrak.hex.network;

import at.petrak.hex.casting.CastingContext;
import at.petrak.hex.casting.CastingHarness;
import at.petrak.hex.casting.CastingHarness.CastResult;
import at.petrak.hex.hexes.HexPattern;
import at.petrak.hex.items.ItemWand;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a MsgNewSpellPatternAck packet
 */
public record MsgNewSpellPatternSyn(int windowID, HexPattern pattern) {
    // Not actually sure if you can check for window ID
    // but i'll leave space for it just in case

    public static MsgNewSpellPatternSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var windowID = buf.readInt();
        var pattern = HexPattern.DeserializeFromNBT(buf.readAnySizeNbt());
        return new MsgNewSpellPatternSyn(windowID, pattern);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeInt(this.windowID);
        buf.writeNbt(this.pattern.serializeToNBT());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) {
                var held = sender.getMainHandItem();
                if (held.getItem() instanceof ItemWand) {
                    var tag = held.getOrCreateTag();
                    var harness = CastingHarness.DeserializeFromNBT(tag, sender);

                    var res = harness.update(this.pattern);
                    if (res instanceof CastResult.Success) {
                        CastResult.Success success = (CastResult.Success) res;
                        success.getSpell().cast(new CastingContext(sender));
                    } else if (res instanceof CastResult.Error) {
                        CastResult.Error error = (CastResult.Error) res;
                        sender.sendMessage(new TextComponent(error.getExn().toString()), Util.NIL_UUID);
                    }

                    var quit = !(res instanceof CastResult.Nothing);
                    HexMessages.getNetwork()
                            .send(PacketDistributor.PLAYER.with(() -> sender),
                                    new MsgNewSpellPatternAck(this.windowID, quit));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
