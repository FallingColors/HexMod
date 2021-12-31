package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import net.minecraft.nbt.CompoundTag;

/**
 * Single-use magic item.
 */
public class ItemCypher extends ItemPackagedSpell {
    public ItemCypher(Properties pProperties) {
        super(pProperties);
    }

    @Override
    boolean singleUse() {
        return true;
    }

    @Override
    int getMaxMana(CompoundTag tag) {
        return HexMod.CONFIG.cypherMaxMana.get();
    }

    @Override
    int getManaRechargeRate(CompoundTag tag) {
        return 0;
    }
}
