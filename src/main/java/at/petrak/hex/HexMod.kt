package at.petrak.hex

import at.petrak.hex.client.RegisterClientStuff
import at.petrak.hex.common.casting.RegisterPatterns
import at.petrak.hex.common.casting.operators.spells.great.OpFlight
import at.petrak.hex.common.items.HexItems
import at.petrak.hex.common.lib.HexSounds
import at.petrak.hex.common.lib.HexStatistics
import at.petrak.hex.common.lib.LibCapabilities
import at.petrak.hex.common.network.HexMessages
import at.petrak.hex.datagen.Advancements
import at.petrak.hex.datagen.DataGenerators
import at.petrak.hex.datagen.LootModifiers
import at.petrak.hex.server.TickScheduler
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(HexMod.MOD_ID)
object HexMod {
    // hmm today I will use a popular logging framework :clueless:
    val LOGGER: Logger = LogManager.getLogger()
    var CONFIG: HexConfig
    var CONFIG_SPEC: ForgeConfigSpec

    const val MOD_ID = "hex"

    init {
        val (cfg, spec) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> HexConfig(builder) }
        CONFIG = cfg
        CONFIG_SPEC = spec

        // mod lifecycle
        val modBus = thedarkcolour.kotlinforforge.forge.MOD_BUS
        // game events
        val evBus = thedarkcolour.kotlinforforge.forge.FORGE_BUS

        modBus.register(this)
        // gotta do it at *some* point
        modBus.register(RegisterPatterns::class.java)
        modBus.register(RegisterClientStuff::class.java)
        modBus.register(DataGenerators::class.java)

        HexItems.ITEMS.register(modBus)
        LootModifiers.LOOT_MODS.register(modBus)
        HexSounds.SOUNDS.register(modBus)

        evBus.register(TickScheduler)
        evBus.register(LibCapabilities::class.java)
        evBus.register(OpFlight)

        // and then things that don't require busses
        HexMessages.register()
        HexStatistics.register()

    }

    @SubscribeEvent
    fun commonSetup(evt: FMLCommonSetupEvent) {
        LOGGER.info("commonSetup")
        evt.enqueueWork { Advancements.registerTriggers() }
    }

    @JvmStatic
    fun getLogger() = this.LOGGER

    @JvmStatic
    fun getConfig() = this.CONFIG

}