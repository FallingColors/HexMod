package at.petrak.hexcasting.forge.recipe;

import at.petrak.hexcasting.api.addldata.ADIotaHolder;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ForgeUnsealedIngredient implements ICustomIngredient {
    public static final ResourceLocation ID = modLoc("unsealed");

    private final ItemStack stack;

    private static ItemStack createStack(ItemStack base) {
        ItemStack newStack = base.copy();
        NBTHelper.putString(newStack, IotaHolderItem.TAG_OVERRIDE_VISUALLY, "any");
        return newStack;
    }

    protected ForgeUnsealedIngredient(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * Creates a new ingredient matching the given stack
     */
    public static ForgeUnsealedIngredient of(ItemStack stack) {
        return new ForgeUnsealedIngredient(stack);
    }

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null) {
            return false;
        }
        if (this.stack.getItem() == input.getItem() && this.stack.getDamageValue() == input.getDamageValue()) {
            ADIotaHolder holder = IXplatAbstractions.INSTANCE.findDataHolder(this.stack);
            if (holder != null) {
                return holder.readIotaTag() != null && holder.writeIota(new NullIota(), true);
            }
        }
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public @NotNull Stream<ItemStack> getItems() {
        return Stream.of(createStack(stack));
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return ForgeHexIngredientTypes.UNSEALED.get();
    }

    public static final MapCodec<ForgeUnsealedIngredient> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
            ResourceLocation.CODEC.xmap(
                rl -> new ItemStack(BuiltInRegistries.ITEM.get(rl)),
                s -> BuiltInRegistries.ITEM.getKey(s.getItem())
            ).fieldOf("item").forGetter(ForgeUnsealedIngredient::getStack)
        ).apply(inst, ForgeUnsealedIngredient::new)
    );

    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, ForgeUnsealedIngredient> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());
}
