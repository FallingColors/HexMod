package at.petrak.hexcasting.common.recipe.ingredient.state;

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

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StateIngredientBlock implements StateIngredient {
    private final Block block;

    public StateIngredientBlock(Block block) {
        this.block = block;
    }

    @Override
    public StateIngredientType<?> getType() {
        return StateIngredients.BLOCK_TYPE;
    }

    @Override
    public boolean test(BlockState blockState) {
        return block == blockState.getBlock();
    }

    @Override
    public BlockState pick(Random random) {
        return block.defaultBlockState();
    }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        if (block.asItem() == Items.AIR) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ItemStack(block));
    }

    @Override
    public List<BlockState> getDisplayed() {
        return Collections.singletonList(block.defaultBlockState());
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return block == ((StateIngredientBlock) o).block;
    }

    @Override
    public int hashCode() {
        return block.hashCode();
    }

    @Override
    public String toString() {
        return "StateIngredientBlock{" + block + "}";
    }


    public static class Type implements StateIngredientType<StateIngredientBlock> {
        public static final MapCodec<StateIngredientBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(StateIngredientBlock::getBlock)
        ).apply(instance, StateIngredientBlock::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlock> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.BLOCK), StateIngredientBlock::getBlock,
                StateIngredientBlock::new
        );

        @Override
        public MapCodec<StateIngredientBlock> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredientBlock> streamCodec() {
            return STREAM_CODEC;
        }
    }
}