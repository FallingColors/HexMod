package at.petrak.hex.common.items;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class ItemManaHolder extends Item {
    public static final String TAG_MANA = "hex.mana";

    public ItemManaHolder(Properties pProperties) {
        super(pProperties);
    }

    abstract int getMaxMana(CompoundTag tag);

    abstract int getManaRechargeRate(CompoundTag tag);

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
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        var tag = pStack.getOrCreateTag();
        tag.putInt(TAG_MANA, Math.min(getMana(tag) + getManaRechargeRate(tag), getMaxMana(tag)));
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
        var maxMana = getMaxMana(tag);
        float amt;
        if (maxMana == 0) {
            amt = 0f;
        } else {
            amt = ((float) mana) / ((float) maxMana);
        }

        var r = Mth.lerp(amt, 149f, 112f);
        var g = Mth.lerp(amt, 196f, 219f);
        var b = Mth.lerp(amt, 174f, 212f);
        return Mth.color(r / 255f, g / 255f, b / 255f);
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        var tag = pStack.getOrCreateTag();
        var mana = getMana(tag);
        var maxMana = getMaxMana(tag);
        float amt;
        if (maxMana == 0) {
            amt = 0f;
        } else {
            amt = ((float) mana) / ((float) maxMana);
        }
        return Math.round(13f * amt);
    }

}
