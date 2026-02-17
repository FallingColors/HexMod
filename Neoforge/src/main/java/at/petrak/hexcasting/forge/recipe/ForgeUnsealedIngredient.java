package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.common.lib.HexDataComponents;
import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

public class ForgeUnsealedIngredient implements ICustomIngredient {
    public static final MapCodec<ForgeUnsealedIngredient> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ItemStack.CODEC.fieldOf("stack").forGetter(ForgeUnsealedIngredient::getStack)
    ).apply(inst, ForgeUnsealedIngredient::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ForgeUnsealedIngredient> STREAM_CODEC = ItemStack.STREAM_CODEC.map(
            ForgeUnsealedIngredient::of,
            ForgeUnsealedIngredient::getStack
    );

    private final ItemStack stack;

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        base.set(HexDataComponents.VISUAL_OVERRIDE, Optional.empty());
        return newStack;
    }

    protected ForgeUnsealedIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public ItemStack getStack() {
        return stack;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static ForgeUnsealedIngredient of(ItemStack stack) {
        return new ForgeUnsealedIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        if (this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue()) {
            ADIotaHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(this.stack);
            if (holder != null) {
                return holder.readIota() != null && holder.writeIota(new NullIota(), true);
            }
        }

        return false;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(createStack(stack));
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return ForgeHexIngredientTypes.UNSEALED_INGREDIENT.get();
    }
}
