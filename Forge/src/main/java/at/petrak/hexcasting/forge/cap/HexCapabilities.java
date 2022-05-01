package at.petrak.hexcasting.forge.cap;

import at.petrak.hexcasting.api.addldata.Colorizer;
import at.petrak.hexcasting.api.addldata.DataHolder;
import at.petrak.hexcasting.api.addldata.HexHolder;
import at.petrak.hexcasting.api.addldata.ManaHolder;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class HexCapabilities {

    public static final Capability<ManaHolder> MANA = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<DataHolder> DATUM = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<HexHolder> STORED_HEX = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final Capability<Colorizer> COLOR = CapabilityManager.get(new CapabilityToken<>() {
    });
}
