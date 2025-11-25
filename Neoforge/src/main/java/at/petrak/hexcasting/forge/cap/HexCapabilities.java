package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public final class HexCapabilities {

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
}
