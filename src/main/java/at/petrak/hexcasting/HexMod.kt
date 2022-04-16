package at.petrak.hexcasting

import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.client.*
import at.petrak.hexcasting.common.blocks.HexBlockEntities
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight
import at.petrak.hexcasting.common.command.HexCommands
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.lib.HexCapabilityHandler
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.particles.HexParticles
import at.petrak.hexcasting.common.recipe.HexCustomRecipes
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.datagen.HexDataGenerators
import at.petrak.hexcasting.datagen.lootmods.HexLootModifiers
import at.petrak.hexcasting.server.TickScheduler
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Mod(HexMod.MOD_ID)
object HexMod {
    // hmm today I will use a popular logging framework :clueless:
    val LOGGER: Logger = LoggerFactory.getLogger(HexMod::class.java)

    var CONFIG_SPEC: ForgeConfigSpec
    var CLIENT_CONFIG_SPEC: ForgeConfigSpec

    // mumblemumble thanks shy mumble mumble
    const val MOD_ID = "hexcasting"

    init {
        val (cfg, spec) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? ->
                HexConfig(
                    builder
                )
            }
        CONFIG_SPEC = spec
        val (client_cfg, client_spec) = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> HexConfig.Client(builder) }
        CLIENT_CONFIG_SPEC = client_spec

        // mod lifecycle
        val modBus = thedarkcolour.kotlinforforge.forge.MOD_BUS
        // game events
        val evBus = thedarkcolour.kotlinforforge.forge.FORGE_BUS

        modBus.register(this)
        // gotta do it at *some* point
        modBus.register(RegisterPatterns::class.java)
        modBus.register(HexDataGenerators::class.java)

        HexItems.ITEMS.register(modBus)
        HexBlocks.BLOCKS.register(modBus)
        HexBlockEntities.BLOCK_ENTITIES.register(modBus)
        HexEntities.ENTITIES.register(modBus)
        HexLootModifiers.LOOT_MODS.register(modBus)
        HexSounds.SOUNDS.register(modBus)
        HexParticles.PARTICLES.register(modBus)
        HexCustomRecipes.RECIPES.register(modBus)
        HexRecipeSerializers.SERIALIZERS.register(modBus)
        modBus.register(HexStatistics::class.java)
        modBus.register(HexRecipeSerializers::class.java)

        evBus.register(HexCommands::class.java)
        evBus.register(TickScheduler)
        evBus.register(HexCapabilityHandler::class.java)
        evBus.register(HexPlayerDataHelper::class.java)
        evBus.register(OpFlight)
        evBus.register(Brainsweeping::class.java)

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
            Runnable {
                modBus.register(RegisterClientStuff::class.java)
                evBus.register(ClientTickCounter::class.java)
                evBus.register(HexAdditionalRenderers::class.java)
                evBus.register(ShiftScrollListener::class.java)
                evBus.register(HexTooltips::class.java)
            }
        }

        // and then things that don't require busses
        HexMessages.register()

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG_SPEC)
    }

    @SubscribeEvent
    fun commonSetup(evt: FMLCommonSetupEvent) {
        evt.enqueueWork { HexAdvancementTriggers.registerTriggers() }
    }

    @JvmStatic
    fun getLogger() = this.LOGGER

    @SubscribeEvent
    fun printPatternCount(evt: FMLLoadCompleteEvent) {
        getLogger().info(
            PatternRegistry.getPatternCountInfo()
        )
    }
}
