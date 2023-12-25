package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import at.petrak.hexcasting.api.client.ClientCastingStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import java.util.function.Supplier;

public final class HexCapabilities {

    public static final Capability<ADMediaHolder> MEDIA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADIotaHolder> IOTA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADHexHolder> STORED_HEX = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADVariantItem> VARIANT_ITEM = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADPigment> COLOR = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<Supplier<ClientCastingStack>> CLIENT_CASTING_STACK = CapabilityManager.get(new CapabilityToken<>() {
    });
}
