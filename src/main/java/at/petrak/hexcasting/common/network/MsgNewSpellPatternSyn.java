package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.casting.CastingContext;
import at.petrak.hexcasting.common.casting.CastingHarness;
import at.petrak.hexcasting.common.casting.CastingHarness.CastResult;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.hexmath.HexPattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
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
                    var harness = CastingHarness.DeserializeFromNBT(tag.getCompound(ItemWand.TAG_HARNESS), sender,
                        this.handUsed);

                    var res = harness.update(this.pattern, sender.getLevel());
                    if (res instanceof CastResult.Cast success) {
                        var castCtx = new CastingContext(sender, this.handUsed);
                        for (var spell : success.getSpells()) {
                            spell.cast(castCtx);
                        }
                        sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
                            HexSounds.ACTUALLY_CAST.get(), SoundSource.PLAYERS, 1f,
                            1f + ((float) Math.random() - 0.5f) * 0.2f);
                    } else if (res instanceof CastResult.Error error) {
                        sender.sendMessage(new TextComponent(error.getExn().getMessage()), Util.NIL_UUID);
                    }

                    CompoundTag nextHarnessTag;
                    if (res.quitStatus() == CastingHarness.QuitStatus.QUIT) {
                        // discard the changes
                        nextHarnessTag = new CompoundTag();
                    } else {
                        // save the changes
                        nextHarnessTag = harness.serializeToNBT();
                    }
                    tag.put(ItemWand.TAG_HARNESS, nextHarnessTag);

                    var descs = new ArrayList<String>(harness.getStack().size());
                    for (var datum : harness.getStack()) {
                        descs.add(datum.display());
                    }

                    HexMessages.getNetwork()
                        .send(PacketDistributor.PLAYER.with(() -> sender),
                            new MsgNewSpellPatternAck(res.quitStatus(), descs));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

}
