package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.lib.HexStateIngredients;
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredient;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public record FreezeRecipe(
    StateIngredient blockIn,
    BlockState result
) implements Recipe<RecipeInput> {
    public boolean matches(BlockState blockIn) {
        return this.blockIn.test(blockIn);
    }

    @Override
    public RecipeType<?> getType() {
        return HexRecipeStuffRegistry.FREEZE_TYPE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return HexRecipeStuffRegistry.FREEZE;
    }

    // in order to get this to be a "Recipe" we need to do a lot of bending-over-backwards
    // to get the implementation to be satisfied even though we never use it
    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY.copy();
    }

    public static class Serializer extends RecipeSerializerBase<FreezeRecipe> {
        public static MapCodec<FreezeRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        HexStateIngredients.TYPED_CODEC.fieldOf("blockIn").forGetter(FreezeRecipe::blockIn),
                        BlockState.CODEC.fieldOf("result").forGetter(FreezeRecipe::result)
                ).apply(inst, FreezeRecipe::new)
        );
        public static StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> STREAM_CODEC = StreamCodec.composite(
                HexStateIngredients.TYPED_STREAM_CODEC, FreezeRecipe::blockIn,
                ByteBufCodecs.VAR_INT, (recipe) -> Block.getId(recipe.result),
                (state, stateId) ->
                        new FreezeRecipe(state, Block.stateById(stateId))
        );

        @Override
        public @NotNull MapCodec<FreezeRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, FreezeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
