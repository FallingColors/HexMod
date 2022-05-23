package at.petrak.hexcasting.forge.mixin;

import com.google.gson.JsonObject;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.RecipeProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;

// FARP
@Mixin(RecipeProvider.class)
public interface ForgeAccessorRecipeProvider {
    @Invoker("saveAdvancement")
    void hex$saveRecipeAdvancement(HashCache dataCache, JsonObject jsonObject, Path path);
}
