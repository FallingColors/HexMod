package at.petrak.hexcasting.common.recipe.ingredient.state;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

public class StateIngredients {
    public static final Codec<StateIngredient> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getStateIngredientRegistry()
            .byNameCodec()
            .dispatch("type", StateIngredient::getType, StateIngredientType::codec));
    public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredient> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.STATE_INGREDIENT)
            .dispatch(StateIngredient::getType, StateIngredientType::streamCodec);

    public static final StateIngredientType<StateIngredientBlock> BLOCK_TYPE = new StateIngredientBlock.Type();
    public static final StateIngredientType<StateIngredientBlockState> BLOCK_STATE = new StateIngredientBlockState.Type();
    public static final StateIngredientType<StateIngredientBlocks> BLOCKS = new StateIngredientBlocks.Type();
    public static final StateIngredientType<StateIngredientTag> TAG = new StateIngredientTag.Type();
    public static final StateIngredientType<StateIngredientTagExcluding> TAG_EXCLUDING = new StateIngredientTagExcluding.Type();

    public static final StateIngredientType<? extends StateIngredient> NONE_TYPE = new StateIngredientType<>() {
        @Override
        public MapCodec<StateIngredient> codec() {
            return MapCodec.unit(NONE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredient> streamCodec() {
            return StreamCodec.unit(NONE);
        }
    };

    public static final StateIngredient NONE = new StateIngredient() {
        @Override
        public boolean test(BlockState state) {
            return true;
        }

        @Override
        public BlockState pick(Random random) {
            throw new UnsupportedOperationException("Should never try to pick from NONE state ingredient");
        }

        @Override
        public StateIngredientType<?> getType() {
            return NONE_TYPE;
        }

        @Override
        public List<ItemStack> getDisplayedStacks() {
            return List.of();
        }

        @Override
        public List<BlockState> getDisplayed() {
            return List.of();
        }
    };

    public static void register(BiConsumer<StateIngredientType<?>, ResourceLocation> r) {
        r.accept(NONE_TYPE, HexAPI.modLoc("none"));
        r.accept(BLOCK_TYPE, HexAPI.modLoc("block"));
        r.accept(BLOCK_STATE, HexAPI.modLoc("state"));
        r.accept(BLOCKS, HexAPI.modLoc("blocks"));
        r.accept(TAG, HexAPI.modLoc("tag"));
        r.accept(TAG_EXCLUDING, HexAPI.modLoc("tag_excluding"));
    }

    public static StateIngredient of(Block block) {
        return new StateIngredientBlock(block);
    }

    public static StateIngredient of(BlockState state) {
        return new StateIngredientBlockState(state);
    }

    public static StateIngredient of(TagKey<Block> tag) {
        return new StateIngredientTag(tag);
    }
}
