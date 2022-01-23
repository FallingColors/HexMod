package at.petrak.hexcasting.common.items.magic;

public class ItemCypher extends ItemPackagedSpell {
    public ItemCypher(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawManaFromInventory() {
        return false;
    }

    @Override
    public boolean singleUse() {
        return true;
    }
}
