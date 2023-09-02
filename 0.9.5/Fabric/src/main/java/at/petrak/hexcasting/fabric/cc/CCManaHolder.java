package at.petrak.hexcasting.fabric.cc;

import at.petrak.hexcasting.api.addldata.ManaHolder;
import at.petrak.hexcasting.api.item.ManaHolderItem;
import dev.onyxstudios.cca.api.v3.item.ItemComponent;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public abstract class CCManaHolder extends ItemComponent implements ManaHolder {
    public CCManaHolder(ItemStack stack) {
        super(stack, HexCardinalComponents.MANA_HOLDER);
    }

    public static class ItemBased extends CCManaHolder {
        private final ManaHolderItem manaHolder;

        public ItemBased(ItemStack stack) {
            super(stack);
            if (!(stack.getItem() instanceof ManaHolderItem mana)) {
                throw new IllegalStateException("item is not a mana holder: " + stack);
            }
            this.manaHolder = mana;
        }

        @Override
        public int getMana() {
            return this.manaHolder.getMana(this.stack);
        }

        @Override
        public int getMaxMana() {
            return this.manaHolder.getMaxMana(this.stack);
        }

        @Override
        public void setMana(int mana) {
            this.manaHolder.setMana(this.stack, mana);
        }

        @Override
        public boolean canRecharge() {
            return this.manaHolder.canRecharge(this.stack);
        }

        @Override
        public boolean canProvide() {
            return this.manaHolder.manaProvider(this.stack);
        }

        @Override
        public int getConsumptionPriority() {
            return 40;
        }

        @Override
        public boolean canConstructBattery() {
            return false;
        }

        @Override
        public int withdrawMana(int cost, boolean simulate) {
            return this.manaHolder.withdrawMana(this.stack, cost, simulate);
        }

        @Override
        public int insertMana(int amount, boolean simulate) {
            return this.manaHolder.insertMana(this.stack, amount, simulate);
        }
    }

    public static class Static extends CCManaHolder {
        private final Supplier<Integer> baseWorth;
        private final int consumptionPriority;

        public Static(Supplier<Integer> baseWorth, int consumptionPriority, ItemStack stack) {
            super(stack);
            this.baseWorth = baseWorth;
            this.consumptionPriority = consumptionPriority;
        }

        @Override
        public int getMana() {
            return baseWorth.get() * stack.getCount();
        }

        @Override
        public int getMaxMana() {
            return getMana();
        }

        @Override
        public void setMana(int mana) {
            // NO-OP
        }

        @Override
        public boolean canRecharge() {
            return false;
        }

        @Override
        public boolean canProvide() {
            return true;
        }

        @Override
        public int getConsumptionPriority() {
            return consumptionPriority;
        }

        @Override
        public boolean canConstructBattery() {
            return true;
        }

        @Override
        public int withdrawMana(int cost, boolean simulate) {
            int worth = baseWorth.get();
            if (cost < 0) {
                cost = worth * stack.getCount();
            }
            double itemsRequired = cost / (double) worth;
            int itemsUsed = Math.min((int) Math.ceil(itemsRequired), stack.getCount());
            if (!simulate) {
                stack.shrink(itemsUsed);
            }
            return itemsUsed * worth;
        }
    }
}
