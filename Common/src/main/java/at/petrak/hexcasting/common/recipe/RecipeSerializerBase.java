package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.annotations.SoftImplement;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

// https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/common/crafting/RecipeSerializerBase.java
// TL;DR Forge bad, so we have to cursed self-mixin
public abstract class RecipeSerializerBase<T extends Recipe<?>> implements RecipeSerializer<T> {
	@Nullable private ResourceLocation registryName;

	@SoftImplement("IForgeRegistryEntry")
	public RecipeSerializerBase<T> setRegistryName(ResourceLocation name) {
		registryName = name;
		return this;
	}

	@SoftImplement("IForgeRegistryEntry")
	@Nullable public ResourceLocation getRegistryName() {
		return registryName;
	}

	@SoftImplement("IForgeRegistryEntry")
	@SuppressWarnings("unchecked")
	public Class<RecipeSerializer<?>> getRegistryType() {
		Class<?> clazz = RecipeSerializer.class;
		return (Class<RecipeSerializer<?>>) clazz;
	}
}
