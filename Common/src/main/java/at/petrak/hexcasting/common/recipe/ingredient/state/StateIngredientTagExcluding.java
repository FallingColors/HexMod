package at.petrak.hexcasting.common.recipe.ingredient.state;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class StateIngredientTagExcluding extends StateIngredientTag {
    private final List<StateIngredient> excludes;

    public StateIngredientTagExcluding(TagKey<Block> tag, Collection<StateIngredient> excludes) {
        super(tag);
        this.excludes = List.copyOf(excludes);
    }

    @Override
    public StateIngredientType<?> getType() {
        return StateIngredients.TAG_EXCLUDING;
    }

    @Override
    public boolean test(BlockState state) {
        if (!super.test(state)) {
            return false;
        }
        return isNotExcluded(state);
    }

    @Override
    public BlockState pick(Random random) {
        List<Block> blocks = getBlocks();
        if (blocks.isEmpty()) {
            return null;
        }
        return blocks.get(random.nextInt(blocks.size())).defaultBlockState();
    }

    private boolean isNotExcluded(BlockState state) {
        for (StateIngredient exclude : excludes) {
            if (exclude.test(state)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) && this.excludes.equals(((StateIngredientTagExcluding) o).excludes);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        return getBlocks().stream()
            .filter(b -> b.asItem() != Items.AIR)
            .map(ItemStack::new)
            .toList();
    }

    @NotNull
    @Override
    protected List<Block> getBlocks() {
        return super.getBlocks().stream()
            .filter(b -> isNotExcluded(b.defaultBlockState()))
            .toList();
    }

    @Override
    public List<BlockState> getDisplayed() {
        return super.getDisplayed().stream()
            .filter(this::isNotExcluded)
            .toList();
    }

    public List<StateIngredient> getExcludes() {
        return excludes;
    }

    public static class Type implements StateIngredientType<StateIngredientTagExcluding> {
        public static final MapCodec<StateIngredientTagExcluding> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TagKey.hashedCodec(Registries.BLOCK).fieldOf("tag").forGetter(StateIngredientTagExcluding::getTag),
                StateIngredients.TYPED_CODEC.listOf().fieldOf("excludes").forGetter(StateIngredientTagExcluding::getExcludes)
        ).apply(instance, StateIngredientTagExcluding::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientTagExcluding> STREAM_CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC.map(id -> TagKey.create(Registries.BLOCK, id), TagKey::location), StateIngredientTagExcluding::getTag,
                StateIngredients.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()), StateIngredientTagExcluding::getExcludes,
                StateIngredientTagExcluding::new
        );

        @Override
        public MapCodec<StateIngredientTagExcluding> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredientTagExcluding> streamCodec() {
            return STREAM_CODEC;
        }
    }
}