package at.petrak.hexcasting.common.items.magic;

import at.petrak.hexcasting.api.misc.ManaConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemMediaBattery extends ItemMediaHolder {
    public static final ResourceLocation MANA_PREDICATE = modLoc("mana");
    public static final ResourceLocation MAX_MANA_PREDICATE = modLoc("max_mana");

    public ItemMediaBattery(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canProvideMedia(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canRecharge(ItemStack stack) {
        return true;
    }

    @Override
    public void fillItemCategory(@NotNull CreativeModeTab tab, @NotNull NonNullList<ItemStack> items) {
        // who was drunk at the wheel when they named this
        if (allowdedIn(tab)) {
            var manamounts = new int[]{
                ManaConstants.CRYSTAL_UNIT,
                20 * ManaConstants.CRYSTAL_UNIT,
                64 * ManaConstants.CRYSTAL_UNIT,
                640 * ManaConstants.CRYSTAL_UNIT,
                6400 * ManaConstants.CRYSTAL_UNIT,
            };
            for (int manamount : manamounts) {
                var stack = new ItemStack(this);
                items.add(ItemMediaHolder.withMana(stack, manamount, manamount));
            }
        }
    }
}
