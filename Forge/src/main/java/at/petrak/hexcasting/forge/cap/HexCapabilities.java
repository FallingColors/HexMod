package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.*;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class HexCapabilities {

    public static final Capability<ADMediaHolder> MANA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADIotaHolder> DATUM = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADHexHolder> STORED_HEX = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADColorizer> COLOR = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<ADCircleWidget> CIRCLE_WIDGET = CapabilityManager.get(new CapabilityToken<>() {
    });
}
