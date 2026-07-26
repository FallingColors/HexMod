package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.recipe.ingredient.state.*;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
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
import java.util.function.Supplier;

public class HexStateIngredients {
    private static final IXplatRegister<StateIngredientType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.STATE_INGREDIENT);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Codec<StateIngredient> TYPED_CODEC = Codec.lazyInitialized(() -> IXplatAbstractions.INSTANCE
            .getStateIngredientRegistry()
            .byNameCodec()
            .dispatch("type", StateIngredient::getType, StateIngredientType::codec));
    public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredient> TYPED_STREAM_CODEC = ByteBufCodecs
            .registry(HexRegistries.STATE_INGREDIENT)
            .dispatch(StateIngredient::getType, StateIngredientType::streamCodec);

    public static final Supplier<StateIngredientType<StateIngredientBlock>> BLOCK_TYPE = REGISTER.register("block", StateIngredientBlock.Type::new);
    public static final Supplier<StateIngredientType<StateIngredientBlockState>> BLOCK_STATE = REGISTER.register("state", StateIngredientBlockState.Type::new);
    public static final Supplier<StateIngredientType<StateIngredientBlocks>> BLOCKS = REGISTER.register("blocks", StateIngredientBlocks.Type::new);
    public static final Supplier<StateIngredientType<StateIngredientTag>> TAG = REGISTER.register("tag", StateIngredientTag.Type::new);
    public static final Supplier<StateIngredientType<StateIngredientTagExcluding>> TAG_EXCLUDING = REGISTER.register("tag_excluding", StateIngredientTagExcluding.Type::new);

    public static final Supplier<StateIngredientType<? extends StateIngredient>> NONE_TYPE = REGISTER.register("none", () -> new StateIngredientType<>() {
        @Override
        public MapCodec<StateIngredient> codec() {
            return MapCodec.unit(NONE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredient> streamCodec() {
            return StreamCodec.unit(NONE);
        }
    });

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
            return NONE_TYPE.get();
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
