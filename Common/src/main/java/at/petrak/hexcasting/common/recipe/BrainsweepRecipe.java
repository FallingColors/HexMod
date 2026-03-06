package at.petrak.hexcasting.common.recipe;

import at.petrak.hexcasting.common.recipe.ingredient.StateIngredient;
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper;
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredient;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

// God I am a horrible person
public record BrainsweepRecipe(
	ResourceLocation id,
	StateIngredient blockIn,
	BrainsweepeeIngredient entityIn,
	long mediaCost,
	BlockState result
) implements Recipe<RecipeInput> {
	public boolean matches(BlockState blockIn, Entity victim, ServerLevel level) {
		return this.blockIn.test(blockIn) && this.entityIn.test(victim, level);
	}

	public ResourceLocation getId() {
		return id;
	}

	@Override
	public RecipeType<?> getType() {
		return HexRecipeStuffRegistry.BRAINSWEEP_TYPE;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return HexRecipeStuffRegistry.BRAINSWEEP;
	}

	// in order to get this to be a "Recipe" we need to do a lot of bending-over-backwards
	// to get the implementation to be satisfied even though we never use it
	@Override
	public boolean matches(RecipeInput pContainer, Level pLevel) {
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput pContainer, HolderLookup.Provider registries) {
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

	// Because kotlin doesn't like doing raw, unchecked types
	// Can't blame it, but that's what we need to do
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static BlockState copyProperties(BlockState original, BlockState copyTo) {
		for (Property prop : original.getProperties()) {
			if (copyTo.hasProperty(prop)) {
				copyTo = copyTo.setValue(prop, original.getValue(prop));
			}
		}

		return copyTo;
	}

	public static class Serializer extends RecipeSerializerBase<BrainsweepRecipe> {
		private static final Codec<StateIngredient> STATE_INGREDIENT_CODEC = Codec.of(
			new Encoder<>() {
				@Override
				public <T> DataResult<T> encode(StateIngredient input, DynamicOps<T> ops, T prefix) {
					try {
						return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, input.serialize()).convert(ops).getValue());
					} catch (Exception e) {
						return DataResult.error(() -> "Failed to encode StateIngredient: " + e.getMessage());
					}
				}
			},
			new Decoder<>() {
				@Override
				public <T> DataResult<com.mojang.datafixers.util.Pair<StateIngredient, T>> decode(DynamicOps<T> ops, T input) {
					try {
						JsonObject obj = new Dynamic<>(ops, input).convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
						return DataResult.success(com.mojang.datafixers.util.Pair.of(
							StateIngredientHelper.deserialize(obj), input));
					} catch (Exception e) {
						return DataResult.error(() -> e.getMessage());
					}
				}
			}
		);

		private static final Codec<BrainsweepeeIngredient> BRAINSWEEPEE_CODEC = Codec.of(
			new Encoder<>() {
				@Override
				public <T> DataResult<T> encode(BrainsweepeeIngredient input, DynamicOps<T> ops, T prefix) {
					try {
						return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, input.serialize()).convert(ops).getValue());
					} catch (Exception e) {
						return DataResult.error(() -> "Failed to encode BrainsweepeeIngredient: " + e.getMessage());
					}
				}
			},
			new Decoder<>() {
				@Override
				public <T> DataResult<com.mojang.datafixers.util.Pair<BrainsweepeeIngredient, T>> decode(DynamicOps<T> ops, T input) {
					try {
						JsonObject obj = new Dynamic<>(ops, input).convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
						return DataResult.success(com.mojang.datafixers.util.Pair.of(
							BrainsweepeeIngredient.deserialize(obj), input));
					} catch (Exception e) {
						return DataResult.error(() -> e.getMessage());
					}
				}
			}
		);

		private static final Codec<BlockState> BLOCKSTATE_JSON_CODEC = Codec.of(
			new Encoder<>() {
				@Override
				public <T> DataResult<T> encode(BlockState input, DynamicOps<T> ops, T prefix) {
					try {
						return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, StateIngredientHelper.serializeBlockState(input)).convert(ops).getValue());
					} catch (Exception e) {
						return DataResult.error(() -> "Failed to encode BlockState: " + e.getMessage());
					}
				}
			},
			new Decoder<>() {
				@Override
				public <T> DataResult<com.mojang.datafixers.util.Pair<BlockState, T>> decode(DynamicOps<T> ops, T input) {
					try {
						JsonObject obj = new Dynamic<>(ops, input).convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
						return DataResult.success(com.mojang.datafixers.util.Pair.of(
							StateIngredientHelper.readBlockState(obj), input));
					} catch (Exception e) {
						return DataResult.error(() -> e.getMessage());
					}
				}
			}
		);

		private static final MapCodec<BrainsweepRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			ResourceLocation.CODEC.fieldOf("id").forGetter(BrainsweepRecipe::id),
			STATE_INGREDIENT_CODEC.fieldOf("blockIn").forGetter(BrainsweepRecipe::blockIn),
			BRAINSWEEPEE_CODEC.fieldOf("entityIn").forGetter(BrainsweepRecipe::entityIn),
			Codec.LONG.fieldOf("cost").forGetter(BrainsweepRecipe::mediaCost),
			BLOCKSTATE_JSON_CODEC.fieldOf("result").forGetter(BrainsweepRecipe::result)
		).apply(inst, BrainsweepRecipe::new));

		@Override
		public MapCodec<BrainsweepRecipe> codec() {
			return MAP_CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, BrainsweepRecipe> streamCodec() {
			return StreamCodec.of(
				(buf, recipe) -> {
					buf.writeResourceLocation(recipe.id);
					recipe.blockIn.write(buf);
					recipe.entityIn.wrapWrite(buf);
					buf.writeVarLong(recipe.mediaCost);
					buf.writeVarInt(Block.getId(recipe.result));
				},
				buf -> {
					var id = buf.readResourceLocation();
					var blockIn = StateIngredientHelper.read(buf);
					var brainsweepeeIn = BrainsweepeeIngredient.read(buf);
					var cost = buf.readVarLong();
					var result = Block.stateById(buf.readVarInt());
					return new BrainsweepRecipe(id, blockIn, brainsweepeeIn, cost, result);
				}
			);
		}
	}
}
