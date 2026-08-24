package at.petrak.hexcasting.common.items.armor;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * To get the armor model in;
 * On forge: cursed self-mixin
 * On fabric: hook in ClientInit
 */
public class ItemRobes extends ArmorItem {
    public final ArmorItem.Type type;

    public ItemRobes(ArmorItem.Type type, Properties properties) {
        super(Holder.direct(HexAPI.instance().robesMaterial()), type, properties);
        this.type = type;
    }

    @SoftImplement("IItemExtension")
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        // TODO: detect variant based on itemstack data
        return HexAPI.modLoc("textures/armor/robes1.png");
    }
}
