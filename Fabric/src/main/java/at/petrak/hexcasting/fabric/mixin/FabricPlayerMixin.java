package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.common.lib.HexAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FabricPlayerMixin extends LivingEntity {
    protected FabricPlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("RETURN"), method = "createAttributes")
    private static void hex$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        var out = cir.getReturnValue();
        out.add(HexAttributes.GRID_ZOOM);
        out.add(HexAttributes.SCRY_SIGHT);
    }
}
