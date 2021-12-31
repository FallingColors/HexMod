package at.petrak.hex;

import at.petrak.hex.common.casting.RegisterSpells;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.network.HexMessages;
import at.petrak.hex.server.TickScheduler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(HexMod.MOD_ID)
public class HexMod {
    // hmm today I will use a popular logging framework :clueless:
    public static final Logger LOGGER = LogManager.getLogger();
    public static final HexConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        final var specPair = new ForgeConfigSpec.Builder().configure(HexConfig::new);
        CONFIG = specPair.getLeft();
        CONFIG_SPEC = specPair.getRight();
    }

    public static final String MOD_ID = "hex";

    public HexMod() {
        // Register ourselves for server and other game events we are interested in
        var evbus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        evbus.register(HexMod.class);

        HexItems.ITEMS.register(evbus);
        HexMessages.register();
        MinecraftForge.EVENT_BUS.register(TickScheduler.INSTANCE);

        evbus.register(RegisterSpells.class);
    }
}
