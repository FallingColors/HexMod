package at.petrak.hexcasting.common.items.magic;

public class ItemTrinket extends ItemPackagedSpell {
    public ItemTrinket(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawManaFromInventory() {
        return false;
    }

    @Override
    public boolean singleUse() {
        return false;
    }
}
