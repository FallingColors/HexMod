package at.petrak.hexcasting.common.recipe.ingredient.state;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StateIngredientTag extends StateIngredientBlocks {
	private final TagKey<Block> tag;

	public StateIngredientTag(TagKey<Block> tag) {
		super(ImmutableSet.of());
		this.tag = tag;
	}

	@Override
	public StateIngredientType<?> getType() {
		return StateIngredients.TAG;
	}

	public Stream<Block> resolve() {
		return StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(tag).spliterator(), false)
			.map(Holder::value);
	}

	@Override
	public boolean test(BlockState state) {
		return state.is(tag);
	}

	@Override
	public BlockState pick(Random random) {
		var values = resolve().toList();
		if (values.isEmpty()) {
			return null;
		}
		return values.get(random.nextInt(values.size())).defaultBlockState();
	}

	@Override
	public List<ItemStack> getDisplayedStacks() {
		return resolve()
			.filter(b -> b.asItem() != Items.AIR)
			.map(ItemStack::new)
			.collect(Collectors.toList());
	}

	@Nonnull
	@Override
	protected List<Block> getBlocks() {
		return resolve().toList();
	}

	@Override
	public List<BlockState> getDisplayed() {
		return resolve().map(Block::defaultBlockState).collect(Collectors.toList());
	}

	public TagKey<Block> getTag() {
		return tag;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return tag.equals(((StateIngredientTag) o).tag);
	}

	@Override
	public int hashCode() {
		return tag.hashCode();
	}

	@Override
	public String toString() {
		return "StateIngredientTag{" + tag + "}";
	}


	public static class Type implements StateIngredientType<StateIngredientTag> {
		public static final MapCodec<StateIngredientTag> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				TagKey.hashedCodec(Registries.BLOCK).fieldOf("tag").forGetter(StateIngredientTag::getTag)
		).apply(instance, StateIngredientTag::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientTag> STREAM_CODEC = StreamCodec.composite(
				ResourceLocation.STREAM_CODEC.map(id -> TagKey.create(Registries.BLOCK, id), TagKey::location), StateIngredientTag::getTag,
				StateIngredientTag::new
		);

		@Override
		public MapCodec<StateIngredientTag> codec() {
			return CODEC;
		}

		@Override
		public StreamCodec<RegistryFriendlyByteBuf, StateIngredientTag> streamCodec() {
			return STREAM_CODEC;
		}
	}
}
