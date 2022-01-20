package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import at.petrak.hex.common.casting.ManaHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemManaBattery extends Item {
    public static final String TAG_MANA = "hex.mana";

    public ItemManaBattery(Properties pProperties) {
        super(pProperties);
    }


    public int getMana(CompoundTag tag) {
        return tag.getInt(TAG_MANA);
    }

    /**
     * Return how much mana we lack
     */
    public int withdrawMana(CompoundTag tag, int cost) {
        var manaHere = getMana(tag);
        var manaLeft = manaHere - cost;
        tag.putInt(TAG_MANA, Math.max(0, manaLeft));
        return Math.max(0, cost - manaHere);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = getMana(tag);
        var maxMana = HexMod.getConfig().batteryMaxMana.get();
        return ManaHelper.INSTANCE.barColor(mana, maxMana);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = getMana(tag);
        var maxMana = HexMod.getConfig().batteryMaxMana.get();
        return ManaHelper.INSTANCE.barWidth(mana, maxMana);
    }

}
