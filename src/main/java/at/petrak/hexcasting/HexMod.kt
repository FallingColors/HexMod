package at.petrak.hexcasting

import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.HexAdditionalRenderers
import at.petrak.hexcasting.client.RegisterClientStuff
import at.petrak.hexcasting.client.ShiftScrollListener
import at.petrak.hexcasting.common.ContributorList
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight
import at.petrak.hexcasting.common.command.HexCommands
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.items.ItemScroll
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.lib.HexStatistics
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.particles.HexParticles
import at.petrak.hexcasting.datagen.Advancements
import at.petrak.hexcasting.datagen.DataGenerators
import at.petrak.hexcasting.datagen.lootmods.HexLootModifiers
import at.petrak.hexcasting.server.TickScheduler
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
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

    // mumblemumble thanks shy mumble mumble
    const val MOD_ID = "hexcasting"

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
        modBus.register(DataGenerators::class.java)

        HexItems.ITEMS.register(modBus)
        HexBlocks.BLOCKS.register(modBus)
        HexBlocks.BLOCK_ENTITIES.register(modBus)
        HexLootModifiers.LOOT_MODS.register(modBus)
        HexSounds.SOUNDS.register(modBus)
        HexParticles.PARTICLES.register(modBus)

        evBus.register(HexCommands::class.java)
        evBus.register(TickScheduler)
        evBus.register(HexCapabilities::class.java)
        evBus.register(OpFlight)
        evBus.register(ItemScroll::class.java)

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
            Runnable {
                modBus.register(RegisterClientStuff::class.java)
                evBus.register(ClientTickCounter::class.java)
                evBus.register(HexAdditionalRenderers::class.java)
                evBus.register(ShiftScrollListener::class.java)
            }
        }


        // and then things that don't require busses
        HexMessages.register()
        HexStatistics.register()
        ContributorList.loadContributors()
    }

    @SubscribeEvent
    fun commonSetup(evt: FMLCommonSetupEvent) {
        evt.enqueueWork { Advancements.registerTriggers() }
    }

    @JvmStatic
    fun getLogger() = this.LOGGER

    @JvmStatic
    fun getConfig() = this.CONFIG

}