package at.petrak.hex.common.items;

import at.petrak.hex.HexMod;
import net.minecraft.nbt.CompoundTag;

/**
 * Multi-use but non-recharging magic item.
 */
public class ItemTrinket extends ItemPackagedSpell {
    public ItemTrinket(Properties pProperties) {
        super(pProperties);
    }

    @Override
    boolean singleUse() {
        return false;
    }

    @Override
    int getMaxMana(CompoundTag tag) {
        return HexMod.CONFIG.trinketMaxMana.get();
    }

    @Override
    int getManaRechargeRate(CompoundTag tag) {
        return 0;
    }
}
