package at.petrak.hexcasting.forge.mixin;

import at.petrak.hexcasting.common.recipe.RecipeSerializerBase;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/mixin/ForgeMixinRecipeSerializerBase.java
@Mixin(value = RecipeSerializerBase.class, remap = false)
public class ForgeMixinCursedRecipeSerializerBase {
    @Shadow
    @Nullable
    private ResourceLocation registryName;

    public Object setRegistryName(ResourceLocation name) {
        registryName = name;
        return this;
    }
}
