package at.petrak.hexcasting.common.recipe.ingredient.brainsweep;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface BrainsweepeeIngredientType<T extends BrainsweepeeIngredient> {

    MapCodec<T> codec();

    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
