package at.petrak.hexcasting.common.items.armor;

import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

/**
 * To get the armor model in;
 * On forge: cursed self-mixin
 * On fabric: hook in ClientInit
 */
public class ItemRobes extends ArmorItem {
    public final EquipmentSlot slot;

    public ItemRobes(EquipmentSlot slot, Properties properties) {
        super(HexAPI.instance().robesMaterial(), slot, properties);
        this.slot = slot;
    }
}
