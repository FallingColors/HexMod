package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.common.lib.HexItems;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class FabricItemEntityMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void tick(CallbackInfo ci) {
        ItemEntity entity = (ItemEntity) (Object) this;
        if (entity.getItem().is(HexItems.SLATE) && HexItems.SLATE.onEntityItemUpdate(entity.getItem(), entity))
            ci.cancel();
    }
}
