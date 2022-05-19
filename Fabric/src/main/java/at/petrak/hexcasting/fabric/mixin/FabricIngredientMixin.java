package at.petrak.hexcasting.fabric.mixin;

import at.petrak.hexcasting.fabric.recipe.FabricUnsealedIngredient;
import com.google.gson.JsonElement;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Ingredient.class)
public class FabricIngredientMixin {
    @Inject(method = "fromJson", at = @At("HEAD"), cancellable = true)
    private static void fromJson(JsonElement jsonElement, CallbackInfoReturnable<Ingredient> cir) {
        Ingredient overridden = FabricUnsealedIngredient.fromJson(jsonElement);
        if (overridden != null)
            cir.setReturnValue(overridden);
    }

    @Inject(method = "fromNetwork", at = @At("HEAD"), cancellable = true)
    private static void fromNetwork(FriendlyByteBuf friendlyByteBuf, CallbackInfoReturnable<Ingredient> cir) {
        Ingredient overridden = FabricUnsealedIngredient.fromNetwork(friendlyByteBuf);
        if (overridden != null)
            cir.setReturnValue(overridden);
    }
}
