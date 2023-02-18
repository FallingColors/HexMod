package at.petrak.hexcasting.fabric.mixin.client;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ElytraLayer.class)
public class FabricMixinElytraLayer {
    @Redirect(
        method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;" +
            "ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
        at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/item/ItemStack;is" +
            "(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean renderIfAltiora(ItemStack stack, Item elytraItem, PoseStack ps, MultiBufferSource mbs, int light,
        LivingEntity entity) {

        if (entity instanceof Player player) {
            var altiora = IXplatAbstractions.INSTANCE.getAltiora(player);
            if (altiora != null) {
                return true;
            }
        }

        return stack.is(elytraItem);
    }
}
