package at.petrak.hexcasting

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.common.blocks.behavior.HexComposting
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight
import at.petrak.hexcasting.common.command.PatternResLocArgument
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.lib.*
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.forge.ForgeHexConfig
import at.petrak.hexcasting.forge.ForgeOnlyEvents
import at.petrak.hexcasting.forge.cap.CapSyncers
import at.petrak.hexcasting.forge.datagen.HexDataGenerators
import at.petrak.hexcasting.forge.datagen.lootmods.HexLootModifiers
import at.petrak.hexcasting.forge.network.ForgePacketHandler
import net.minecraft.commands.synchronization.ArgumentTypes
import net.minecraft.commands.synchronization.EmptyArgumentSerializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.living.LivingConversionEvent
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import org.apache.logging.log4j.Logger
import java.util.function.BiConsumer
import java.util.function.Consumer


@Mod(HexAPI.MOD_ID)
object ForgeHexInitializer {
    init {
        initConfig()

        initRegistry()
        initListeners()

        ArgumentTypes.register(
            "hexcasting:pattern",
            PatternResLocArgument::class.java,
            EmptyArgumentSerializer { PatternResLocArgument.id() }
        )
        HexAdvancementTriggers.registerTriggers()
    }

    fun initConfig() {
        val config = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig(builder) }
        val serverConfig = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig.Server(builder) }
        val clientConfig = ForgeConfigSpec.Builder()
            .configure { builder: ForgeConfigSpec.Builder? -> ForgeHexConfig.Client(builder) }
        HexConfig.setCommon(config.left)
        HexConfig.setClient(clientConfig.left)
        HexConfig.setServer(serverConfig.left)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverConfig.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfig.right)
    }

    fun initRegistry() {
        bind(ForgeRegistries.SOUND_EVENTS, HexSounds::registerSounds)
        bind(ForgeRegistries.BLOCKS, HexBlocks::registerBlocks)
        bind(ForgeRegistries.ITEMS, HexBlocks::registerBlockItems)
        bind(ForgeRegistries.BLOCK_ENTITIES, HexBlockEntities::registerTiles)
        bind(ForgeRegistries.ITEMS, HexItems::registerItems)

        bind(ForgeRegistries.RECIPE_SERIALIZERS, HexRecipeSerializers::registerSerializers)

        bind(ForgeRegistries.ENTITIES, HexEntities::registerEntities)

        bind(ForgeRegistries.PARTICLE_TYPES, HexParticles::registerParticles)

        val modBus = thedarkcolour.kotlinforforge.forge.MOD_BUS
        HexLootModifiers.LOOT_MODS.register(modBus)
    }

    fun initListeners() {
        // mod lifecycle
        val modBus = thedarkcolour.kotlinforforge.forge.MOD_BUS
        // game events
        val evBus = thedarkcolour.kotlinforforge.forge.FORGE_BUS

        modBus.register(ForgeHexClientInitializer::class.java)

        modBus.addListener { evt: FMLCommonSetupEvent ->
            evt.enqueueWork {
                ForgePacketHandler.init()
                HexComposting.setup()
                HexStrippables.init()
            }
        }

        // We have to do these at some point when the registries are still open
        modBus.addGenericListener(Item::class.java) { _: RegistryEvent<Item> ->
            HexRecipeSerializers.registerTypes()
            HexStatistics.register()
        }

        modBus.addListener { _: FMLLoadCompleteEvent ->
            RegisterPatterns.registerPatterns()
            getLogger().info(
                PatternRegistry.getPatternCountInfo()
            )
        }

        evBus.addListener { evt: EntityInteract ->
            val res = Brainsweeping.tradeWithVillager(
                evt.player, evt.world, evt.hand, evt.target,
                null
            )
            if (res.consumesAction()) {
                evt.isCanceled = true
                evt.cancellationResult = res
            }
        }
        evBus.addListener { evt: LivingConversionEvent.Post ->
            Brainsweeping.copyBrainsweepFromVillager(
                evt.entityLiving, evt.outcome
            )
        }

        evBus.addListener { evt: LivingEvent.LivingUpdateEvent ->
            OpFlight.tickDownFlight(evt.entityLiving)
        }

        evBus.addListener { evt: RegisterCommandsEvent ->
            HexCommands.register(evt.dispatcher)
        }

        modBus.register(HexDataGenerators::class.java)
        evBus.register(CapSyncers::class.java)
        evBus.register(ForgeOnlyEvents::class.java)

    }

    private fun <T : IForgeRegistryEntry<T>> bind(
        registry: IForgeRegistry<T>,
        source: Consumer<BiConsumer<T, ResourceLocation>>
    ) {
        thedarkcolour.kotlinforforge.forge.MOD_BUS.addGenericListener(
            registry.registrySuperType
        ) { event: RegistryEvent.Register<T> ->
            val forgeRegistry = event.registry
            source.accept { t, rl ->
                t.registryName = rl
                forgeRegistry.register(t)
            }
        }
    }

    @JvmStatic
    fun getLogger(): Logger = HexAPI.LOGGER
}
