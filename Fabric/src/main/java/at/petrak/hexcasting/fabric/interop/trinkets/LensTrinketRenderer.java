package at.petrak.hexcasting.fabric.interop.trinkets;

import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * @author WireSegal
 * Created at 9:50 AM on 7/25/22.
 */
public class LensTrinketRenderer implements TrinketRenderer {
    @Override
    @SuppressWarnings("unchecked")
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntity> model,
                       PoseStack matrices, MultiBufferSource multiBufferSource, int light, LivingEntity entity,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress,
                       float headYaw, float headPitch) {
        if (stack.is(HexItems.SCRYING_LENS) &&
            model instanceof PlayerModel playerModel &&
            entity instanceof AbstractClientPlayer player) {

            // from https://github.com/Creators-of-Create/Create/blob/ee33823ed0b5084af10ed131a1626ce71db4c07e/src/main/java/com/simibubi/create/compat/curios/GogglesCurioRenderer.java

            // Translate and rotate with our head
            matrices.pushPose();
            TrinketRenderer.followBodyRotations(entity, playerModel);
            TrinketRenderer.translateToFace(matrices, playerModel, player, headYaw, headPitch);

            // Translate and scale to our head
            matrices.translate(0, 0, 0.3);
            matrices.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
            matrices.scale(0.625f, 0.625f, 0.625f);

            // Render
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.HEAD,
                light, OverlayTexture.NO_OVERLAY, matrices, multiBufferSource, 0);
            matrices.popPose();
        }
    }

}
