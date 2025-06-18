package at.petrak.hexcasting.common.recipe.ingredient.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public interface StateIngredientType<T extends StateIngredient> {

    MapCodec<T> codec();

    StreamCodec<RegistryFriendlyByteBuf, T> streamCodec();
}
