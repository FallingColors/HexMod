package at.petrak.hexcasting.common.recipe.ingredient.state;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

public class StateIngredientBlocks implements StateIngredient {
    protected final ImmutableSet<Block> blocks;

    public StateIngredientBlocks(Collection<Block> blocks) {
        this.blocks = ImmutableSet.copyOf(blocks);
    }

    @Override
    public StateIngredientType<?> getType() {
        return StateIngredients.BLOCKS;
    }

    @Override
    public boolean test(BlockState state) {
        return blocks.contains(state.getBlock());
    }

    @Override
    public BlockState pick(Random random) {
        return blocks.asList().get(random.nextInt(blocks.size())).defaultBlockState();
    }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        return blocks.stream()
            .filter(b -> b.asItem() != Items.AIR)
            .map(ItemStack::new)
            .collect(Collectors.toList());
    }

    @Override
    public List<BlockState> getDisplayed() {
        return blocks.stream().map(Block::defaultBlockState).collect(Collectors.toList());
    }

    @Nonnull
    protected List<Block> getBlocks() {
        return blocks.asList();
    }

    @Override
    public String toString() {
        return "StateIngredientBlocks{" + blocks.toString() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return blocks.equals(((StateIngredientBlocks) o).blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocks);
    }


    public static class Type implements StateIngredientType<StateIngredientBlocks> {
        public static final MapCodec<StateIngredientBlocks> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().listOf().fieldOf("block").forGetter(StateIngredientBlocks::getBlocks)
        ).apply(instance, StateIngredientBlocks::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlocks> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK).apply(ByteBufCodecs.list()), StateIngredientBlocks::getBlocks,
                StateIngredientBlocks::new
        );

        @Override
        public MapCodec<StateIngredientBlocks> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlocks> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
