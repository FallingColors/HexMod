package at.petrak.hex.client;

import at.petrak.hex.HexUtils;
import at.petrak.hex.casting.CastingHarness;
import at.petrak.hex.items.HexItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// https://github.com/gamma-delta/VCC/blob/master/src/main/java/me/gammadelta/client/VCCRenderOverlays.java⌈
public class HexRenderOverlays {
    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent e) {
        LocalPlayer player = Minecraft.getInstance().player;

        ItemStack held = player.getMainHandItem();
        if (held.getItem() == HexItems.wand.get()) {
            tryRenderCastOverlay(e, player, held);
        }
    }

    private static void tryRenderCastOverlay(RenderGameOverlayEvent e, LocalPlayer player, ItemStack wand) {
        if (wand.hasTag() && !wand.getTag().isEmpty()) {
            CompoundTag tag = wand.getTag();
            if (tag.contains(CastingHarness.TAG_POINTS)) {
                PoseStack ps = e.getMatrixStack();
                Minecraft mc = Minecraft.getInstance();
                MultiBufferSource buffers = mc.renderBuffers().bufferSource();


                Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
                Vec3 eyePos = player.getEyePosition(e.getPartialTicks());

                ps.pushPose();

                ps.translate(camPos.x, camPos.y, camPos.z);
                // comment from when i tried to do this in VCC says I need this
                // the chain of trust goes back to eutro
                // *shudders*
                ps.translate(-eyePos.x, -eyePos.y, -eyePos.z);

                ListTag tagPointBlobs = tag.getList(CastingHarness.TAG_POINTS, Tag.TAG_LIST);
                for (int patIdx = 0; patIdx < tagPointBlobs.size(); patIdx++) {
                    ListTag tagPoints = tagPointBlobs.getList(patIdx);
                    // Start new line
                    VertexConsumer buf = buffers.getBuffer(RenderType.LINES);
                    for (int idx = 0; idx < tagPoints.size(); idx++) {
                        // getLongArray is borken, who knew
                        Vec3 here = HexUtils.deserializeVec3FromNBT(
                                ((LongArrayTag) tagPoints.get(idx)).getAsLongArray());
                        addVertex(ps, buf, here);

                        if (idx == tagPoints.size() - 1 &&
                                patIdx == tagPointBlobs.size() - 1 &&
                                tag.contains(CastingHarness.TAG_PDS) &&
                                !tag.getCompound(CastingHarness.TAG_PDS)
                                        .contains(CastingHarness.PatternDrawState.TAG_BETWEEN_PATTERNS)) {
                            // Draw the final line to the player cursor
                            VertexConsumer buf1 = buffers.getBuffer(RenderType.LINES);
                            addVertex(ps, buf1, player.position().add(player.getLookAngle()));
                        }

                    }
                }
                ps.popPose();
            }

        }
    }

    private static void addVertex(PoseStack ps, VertexConsumer buf, Vec3 vert) {
        buf.vertex(ps.last().pose(), (float) vert.x, (float) vert.y, (float) vert.z)
                .color(128, 128, 255, 255)
                .endVertex();
    }
}
