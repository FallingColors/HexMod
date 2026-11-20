package at.petrak.hexcasting.fabric.lib;

import at.petrak.hexcasting.api.addldata.*;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexItemApiLookups {
    public static final ItemApiLookup<ADMediaHolder, Void> MEDIA_HOLDER_LOOKUP = ItemApiLookup.get(modLoc("media_holder_item"), ADMediaHolder.class, Void.class);

    public static final ItemApiLookup<ADIotaHolder, Void> IOTA_HOLDER_LOOKUP = ItemApiLookup.get(modLoc("iota_holder_item"), ADIotaHolder.class, Void.class);

    public static final ItemApiLookup<ADPigment, Void> PIGMENT_ITEM_LOOKUP = ItemApiLookup.get(modLoc("pigment_item"), ADPigment.class, Void.class);

    public static final ItemApiLookup<ADHexHolder, Void> HEX_HOLDER_LOOKUP = ItemApiLookup.get(modLoc("hex_holder_item"), ADHexHolder.class, Void.class);

    public static final ItemApiLookup<ADVariantItem, Void> VARIANT_ITEM_LOOKUP = ItemApiLookup.get(modLoc("variant_item"), ADVariantItem.class, Void.class);
}
