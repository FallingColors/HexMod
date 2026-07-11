package at.petrak.hexcasting.common.recipe.ingredient.state;

import at.petrak.hexcasting.common.lib.HexStateIngredients;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StateIngredientFluid implements StateIngredient {
    private final Fluid fluid;
    
    public StateIngredientFluid(Fluid fluid) { this.fluid = fluid; }

    @Override
    public StateIngredientType<?> getType() {
        return HexStateIngredients.FLUID_TYPE;
    }

    @Override
    public boolean test(BlockState blockState) { return fluid == blockState.getFluidState().getType(); }

    @Override
    public BlockState pick(Random random) { return fluid.defaultFluidState().createLegacyBlock(); }

    @Override
    public List<ItemStack> getDisplayedStacks() {
        if (fluid == Fluids.EMPTY) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new ItemStack(fluid.getBucket()));
    }

    @Override
    public List<BlockState> getDisplayed() {
        return Collections.singletonList(fluid.defaultFluidState().createLegacyBlock());
    }

    public Fluid getFluid() { return fluid; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return fluid == ((StateIngredientFluid) o).fluid;
    }

    @Override
    public int hashCode() {
        return fluid.hashCode();
    }

    @Override
    public String toString() {
        return "StateIngredientBlock{" + fluid + "}";
    }

    public static class Type implements StateIngredientType<StateIngredientFluid> {
        public static final MapCodec<StateIngredientFluid> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(StateIngredientFluid::getFluid)
        ).apply(instance, StateIngredientFluid::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, StateIngredientFluid> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.FLUID), StateIngredientFluid::getFluid,
                StateIngredientFluid::new
        );

        @Override
        public MapCodec<StateIngredientFluid> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StateIngredientFluid> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
