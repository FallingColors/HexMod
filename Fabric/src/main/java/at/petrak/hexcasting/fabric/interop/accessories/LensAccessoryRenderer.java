package at.petrak.hexcasting.fabric.interop.accessories;

import at.petrak.hexcasting.common.lib.HexItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * @author WireSegal
 * Created at 9:50 AM on 7/25/22.
 */
public class LensAccessoryRenderer implements AccessoryRenderer {
    @Override
    @SuppressWarnings("unchecked")
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference slotReference, PoseStack matrices, EntityModel<M> model, MultiBufferSource multiBufferSource, int light, float v, float v1, float v2, float v3, float v4, float v5) {
        if (stack.is(HexItems.SCRYING_LENS) &&
                model instanceof PlayerModel playerModel && slotReference.entity() instanceof Player) {

            // from https://github.com/Creators-of-Create/Create/blob/ee33823ed0b5084af10ed131a1626ce71db4c07e/src/main/java/com/simibubi/create/compat/curios/GogglesCurioRenderer.java

            // Translate and rotate with our head
            matrices.pushPose();

            // Translate and scale to our head
            matrices.translate(0, 0, 0.3);
            matrices.mulPose(Axis.ZP.rotationDegrees(180.0f));
            matrices.scale(0.625f, 0.625f, 0.625f);

            // Render
            var instance = Minecraft.getInstance();
            instance.getItemRenderer().renderStatic(stack, ItemDisplayContext.HEAD,
                    light, OverlayTexture.NO_OVERLAY, matrices, multiBufferSource, instance.level, 0);
            matrices.popPose();
        }
    }
}
