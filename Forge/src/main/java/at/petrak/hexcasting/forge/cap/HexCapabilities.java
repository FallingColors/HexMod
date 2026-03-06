package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public final class HexCapabilities {

    public static final ItemCapability<ADMediaHolder, Void> MEDIA =
        ItemCapability.createVoid(modLoc("media"), ADMediaHolder.class);
    public static final ItemCapability<ADIotaHolder, Void> IOTA =
        ItemCapability.createVoid(modLoc("iota"), ADIotaHolder.class);
    public static final EntityCapability<ADIotaHolder, Void> IOTA_ENTITY =
        EntityCapability.createVoid(modLoc("iota_entity"), ADIotaHolder.class);
    public static final ItemCapability<ADHexHolder, Void> STORED_HEX =
        ItemCapability.createVoid(modLoc("stored_hex"), ADHexHolder.class);
    public static final ItemCapability<ADVariantItem, Void> VARIANT_ITEM =
        ItemCapability.createVoid(modLoc("variant_item"), ADVariantItem.class);
    public static final ItemCapability<ADPigment, Void> COLOR =
        ItemCapability.createVoid(modLoc("pigment"), ADPigment.class);
    @SuppressWarnings("unchecked")
    public static final EntityCapability<Supplier<ClientCastingStack>, Void> CLIENT_CASTING_STACK =
        (EntityCapability<Supplier<ClientCastingStack>, Void>) (Object) EntityCapability.createVoid(
            modLoc("client_casting_stack"), Supplier.class);
}
