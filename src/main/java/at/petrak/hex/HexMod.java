package at.petrak.hex;

import at.petrak.hex.common.casting.RegisterPatterns;
import at.petrak.hex.common.casting.operators.spells.great.OpFlight;
import at.petrak.hex.common.items.HexItems;
import at.petrak.hex.common.lib.HexSounds;
import at.petrak.hex.common.lib.HexStatistics;
import at.petrak.hex.common.lib.LibCapabilities;
import at.petrak.hex.common.network.HexMessages;
import at.petrak.hex.datagen.Advancements;
import at.petrak.hex.datagen.LootModifiers;
import at.petrak.hex.server.TickScheduler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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
        LootModifiers.LOOT_MODS.register(evbus);
        HexSounds.SOUNDS.register(evbus);

        MinecraftForge.EVENT_BUS.register(TickScheduler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LibCapabilities.class);

        MinecraftForge.EVENT_BUS.register(OpFlight.INSTANCE);

        HexMessages.register();
        HexStatistics.register();

        evbus.register(RegisterPatterns.class);
    }

    @SubscribeEvent
    public static void commonSetup(final FMLCommonSetupEvent evt) {
        evt.enqueueWork(() -> {
            Advancements.registerTriggers();
        });
    }
}
