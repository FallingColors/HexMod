package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.common.lib.HexAttributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class FabricPlayerMixin {
    // This is done rather than using FabricDefaultAttributeRegistry.register(EntityType.PLAYER, <attributes>) because
    // if multiple mods all try to use that method, whichever loads last will override the registered AttributeSupplier
    // for EntityType.PLAYER and the other mods' attributes won't get added to the player.
    @Inject(at = @At("RETURN"), method = "createAttributes")
    private static void hex$addAttributes(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
        var out = cir.getReturnValue();
        out.add(HexAttributes.GRID_ZOOM);
        out.add(HexAttributes.SCRY_SIGHT);
        out.add(HexAttributes.FEEBLE_MIND);
        out.add(HexAttributes.MEDIA_CONSUMPTION_MODIFIER);
        out.add(HexAttributes.AMBIT_RADIUS);
        out.add(HexAttributes.SENTINEL_RADIUS);
    }
}