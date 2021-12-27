package at.petrak.hex;

import at.petrak.hex.client.HexRenderOverlays;
import at.petrak.hex.items.HexItems;
import at.petrak.hex.network.HexMessages;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HexMod.MOD_ID)
public class HexMod {
    // hmm today I will use a popular logging framework :clueless:
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "hex";

    public HexMod() {
        // Register ourselves for server and other game events we are interested in
        var evbus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        HexItems.ITEMS.register(evbus);
        MinecraftForge.EVENT_BUS.register(HexRenderOverlays.class);
        HexMessages.register();
    }
}
