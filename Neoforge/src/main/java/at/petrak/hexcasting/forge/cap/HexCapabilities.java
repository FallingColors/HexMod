package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
<<<<<<< HEAD
import at.petrak.hexcasting.api.client.ClientCastingStack;
=======
>>>>>>> refs/remotes/slava/devel/port-1.21
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public final class HexCapabilities {

<<<<<<< HEAD
    public static final ItemCapability<ADMediaHolder, Void> MEDIA =
            ItemCapability.createVoid(
                    modLoc("item_media_holder"),
                    ADMediaHolder.class
            );
    public static final EntityCapability<ADMediaHolder, Void> MEDIA_ENTITY =
            EntityCapability.createVoid(
                    modLoc("entity_media_holder"),
                    ADMediaHolder.class
            );

    public static final ItemCapability<ADIotaHolder, Void> IOTA = ItemCapability.createVoid(
            modLoc("item_iota_holder"),
            ADIotaHolder.class
    );
    public static final EntityCapability<ADIotaHolder, Void> IOTA_ENTITY = EntityCapability.createVoid(
            modLoc("entity_iota_holder"),
            ADIotaHolder.class
    );

    public static final ItemCapability<ADHexHolder, Void> STORED_HEX = ItemCapability.createVoid(
            modLoc("item_hex_holder"),
            ADHexHolder.class
    );

    public static final ItemCapability<ADPigment, Void> COLOR = ItemCapability.createVoid(
            modLoc("item_pigment"),
            ADPigment.class
    );
=======
    public static final class Item {
        public static final ItemCapability<ADMediaHolder, Void> MEDIA = ItemCapability.createVoid(modLoc("media_holder"), ADMediaHolder.class);
        public static final ItemCapability<ADIotaHolder, Void> IOTA = ItemCapability.createVoid(modLoc("iota_holder"), ADIotaHolder.class);
        public static final ItemCapability<ADHexHolder, Void> STORED_HEX = ItemCapability.createVoid(modLoc("hex_holder"), ADHexHolder.class);
        public static final ItemCapability<ADVariantItem, Void> VARIANT_ITEM = ItemCapability.createVoid(modLoc("variant"), ADVariantItem.class);
        public static final ItemCapability<ADPigment, Void> COLOR = ItemCapability.createVoid(modLoc("color"), ADPigment.class);
    }

    public static final class Entity {
        public static final EntityCapability<ADMediaHolder, Void> MEDIA = EntityCapability.createVoid(modLoc("media_holder"), ADMediaHolder.class);
        public static final EntityCapability<ADIotaHolder, Void> IOTA = EntityCapability.createVoid(modLoc("iota_holder"), ADIotaHolder.class);
        public static final EntityCapability<ADHexHolder, Void> STORED_HEX = EntityCapability.createVoid(modLoc("hex_holder"), ADHexHolder.class);
        public static final EntityCapability<ADVariantItem, Void> VARIANT_ITEM = EntityCapability.createVoid(modLoc("variant"), ADVariantItem.class);
        public static final EntityCapability<ADPigment, Void> COLOR = EntityCapability.createVoid(modLoc("color"), ADPigment.class);
    }
>>>>>>> refs/remotes/slava/devel/port-1.21
}
