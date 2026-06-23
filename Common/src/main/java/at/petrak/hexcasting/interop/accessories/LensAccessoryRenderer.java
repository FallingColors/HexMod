package at.petrak.hexcasting.interop.accessories;

import com.mojang.blaze3d.vertex.PoseStack;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.Side;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LensAccessoryRenderer implements SimpleAccessoryRenderer {
    @Override
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, EntityModel<M> model, PoseStack matrices) {
        if (model instanceof PlayerModel<M> playerModel) {
            // Translate and scale to place the lens over the left eye
            AccessoryRenderer.transformToFace(matrices, playerModel.head, Side.FRONT);
            matrices.translate(0.3, 0, 0);
            matrices.scale(0.625f, 0.625f, 0.625f);
        }
    }
}
