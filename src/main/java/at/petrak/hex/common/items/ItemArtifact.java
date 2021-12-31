package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import net.minecraft.nbt.CompoundTag;

/**
 * Multi-use recharging magic item.
 */
public class ItemArtifact extends ItemPackagedSpell {
    public ItemArtifact(Properties pProperties) {
        super(pProperties);
    }

    @Override
    boolean singleUse() {
        return false;
    }

    @Override
    int getMaxMana(CompoundTag tag) {
        return HexMod.CONFIG.artifactMaxMana.get();
    }

    @Override
    int getManaRechargeRate(CompoundTag tag) {
        return HexMod.CONFIG.artifactRechargeRate.get();
    }
}
