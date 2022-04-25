package at.petrak.hexcasting

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import at.petrak.hexcasting.client.*
import at.petrak.hexcasting.common.blocks.HexBlockEntities
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight
import at.petrak.hexcasting.common.command.HexCommands
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.lib.HexCapabilityHandler
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.particles.HexParticles
import at.petrak.hexcasting.common.recipe.HexComposting
import at.petrak.hexcasting.common.recipe.HexCustomRecipes
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.datagen.HexDataGenerators
import at.petrak.hexcasting.datagen.lootmods.HexLootModifiers
import at.petrak.hexcasting.forge.ForgeHexConfig
import at.petrak.hexcasting.server.TickScheduler
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import org.apache.logging.log4j.Logger

@Mod(HexMod.MOD_ID)
object HexMod {

    // mumblemumble thanks shy mumble mumble
    const val MOD_ID = "hexcasting"

    init {
        IXplatAbstractions.INSTANCE.init()

        val config = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig(builder) }
        val serverConfig = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig.Server(builder) }
        val clientConfig = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig.Client(builder) }
        HexConfig.setCommon(config.left)
        HexConfig.setClient(clientConfig.left)
        HexConfig.setServer(serverConfig.left)

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

        modBus.register(HexComposting::class.java)

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

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverConfig.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfig.right)
    }

    @SubscribeEvent
    fun commonSetup(evt: FMLCommonSetupEvent) {
        evt.enqueueWork { HexAdvancementTriggers.registerTriggers() }
    }

    @JvmStatic
    fun getLogger(): Logger = HexAPI.LOGGER

    @SubscribeEvent
    fun printPatternCount(evt: FMLLoadCompleteEvent) {
        getLogger().info(
            PatternRegistry.getPatternCountInfo()
        )
    }
}
