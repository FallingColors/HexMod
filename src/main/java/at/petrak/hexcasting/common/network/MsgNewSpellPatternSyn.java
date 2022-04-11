package at.petrak.hexcasting.common.network;

import at.petrak.hexcasting.common.casting.*;
import at.petrak.hexcasting.common.items.ItemWand;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.hexmath.HexCoord;
import at.petrak.hexcasting.hexmath.HexPattern;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Sent client->server when the player finishes drawing a pattern.
 * Server will send back a MsgNewSpellPatternAck packet
 */
public record MsgNewSpellPatternSyn(InteractionHand handUsed, HexPattern pattern, List<ResolvedPattern> resolvedPatterns) {
    public static MsgNewSpellPatternSyn deserialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        var hand = buf.readEnum(InteractionHand.class);
        var pattern = HexPattern.DeserializeFromNBT(buf.readAnySizeNbt());

        var resolvedPatternsLen = buf.readInt();
        var resolvedPatterns = new ArrayList<ResolvedPattern>(resolvedPatternsLen);
        for (int i = 0; i < resolvedPatternsLen; i++) {
            resolvedPatterns.add(ResolvedPattern.DeserializeFromNBT(buf.readAnySizeNbt()));
        }
        return new MsgNewSpellPatternSyn(hand, pattern, resolvedPatterns);
    }

    public void serialize(ByteBuf buffer) {
        var buf = new FriendlyByteBuf(buffer);
        buf.writeEnum(handUsed);
        buf.writeNbt(this.pattern.serializeToNBT());
        buf.writeInt(this.resolvedPatterns.size());
        for (var pat : this.resolvedPatterns) {
            buf.writeNbt(pat.serializeToNBT());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> networkCtx) {
        networkCtx.get().enqueueWork(() -> {
            ServerPlayer sender = networkCtx.get().getSender();
            if (sender != null) {
                var held = sender.getItemInHand(this.handUsed);
                if (held.getItem() instanceof ItemWand) {
                    boolean autoFail = false;

                    if (!resolvedPatterns.isEmpty()) {
                        var allPoints = new ArrayList<HexCoord>();
                        for (int i = 0; i < resolvedPatterns.size() - 1; i++) {
                            ResolvedPattern pat = resolvedPatterns.get(i);
                            allPoints.addAll(pat.getPattern().positions(pat.getOrigin()));
                        }
                        var currentResolvedPattern = resolvedPatterns.get(resolvedPatterns.size() - 1);
                        var currentSpellPoints = currentResolvedPattern.getPattern().positions(currentResolvedPattern.getOrigin());
                        if (currentSpellPoints.stream().anyMatch(allPoints::contains))
                            autoFail = true;
                    }

                    var ctx = new CastingContext(sender, this.handUsed);
                    var tag = sender.getPersistentData();
                    var harness = CastingHarness.DeserializeFromNBT(tag.getCompound(ItemWand.TAG_HARNESS), ctx);

                    ControllerInfo clientInfo;
                    if (autoFail) {
                        clientInfo = new ControllerInfo(false, false, harness.getStack().isEmpty(), true, harness.generateDescs());
                    } else {
                        clientInfo = harness.executeNewPattern(this.pattern, sender.getLevel());

                        if (clientInfo.getWasSpellCast() && clientInfo.getHasCastingSound()) {
                            sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(),
                                    HexSounds.ACTUALLY_CAST.get(), SoundSource.PLAYERS, 1f,
                                    1f + ((float) Math.random() - 0.5f) * 0.2f);
                        }
                    }

                    ListTag patterns = new ListTag();

                    CompoundTag nextHarnessTag;
                    if (clientInfo.isStackClear()) {
                        // discard the changes
                        nextHarnessTag = new CompoundTag();
                    } else {
                        // save the changes
                        nextHarnessTag = harness.serializeToNBT();
                        if (!resolvedPatterns.isEmpty()) {
                            resolvedPatterns.get(resolvedPatterns.size() - 1)
                                    .setValid(clientInfo.getWasPrevPatternInvalid() ?
                                            ResolvedPatternValidity.ERROR : ResolvedPatternValidity.OK);
                        }
                        for (var pat : resolvedPatterns) {
                            patterns.add(pat.serializeToNBT());
                        }
                    }

                    tag.put(ItemWand.TAG_HARNESS, nextHarnessTag);
                    tag.put(ItemWand.TAG_PATTERNS, patterns);

                    HexMessages.getNetwork()
                            .send(PacketDistributor.PLAYER.with(() -> sender), new MsgNewSpellPatternAck(clientInfo));
                }
            }
        });
        networkCtx.get().setPacketHandled(true);
    }

}
