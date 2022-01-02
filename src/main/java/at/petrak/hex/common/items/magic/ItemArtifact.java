package at.petrak.hex.common.items.magic;

public class ItemArtifact extends ItemPackagedSpell {
    public ItemArtifact(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canDrawManaFromInventory() {
        return true;
    }

    @Override
    public boolean singleUse() {
        return false;
    }
}
