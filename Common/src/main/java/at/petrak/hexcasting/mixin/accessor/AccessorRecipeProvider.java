package at.petrak.hexcasting.mixin.accessor;

import com.google.gson.JsonObject;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.RecipeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

@Mixin(RecipeProvider.class)
public interface AccessorRecipeProvider {
    @Invoker("saveRecipe")
    static void hex$SaveRecipe(HashCache $$0, JsonObject $$1, Path $$2) {
    }
}
