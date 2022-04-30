package at.petrak.hexcasting

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.PatternRegistry
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.blocks.HexBlocks
import at.petrak.hexcasting.common.items.HexItems
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.forge.ForgeHexConfig
import at.petrak.hexcasting.forge.cap.CapSyncers
import at.petrak.hexcasting.forge.network.ForgePacketHandler
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import org.apache.logging.log4j.Logger
import java.util.function.BiConsumer
import java.util.function.Consumer


@Mod(HexAPI.MOD_ID)
object ForgeHexInitializer {
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
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, config.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverConfig.right)
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientConfig.right)

        initRegistry()
        initListeners()
    }

    fun initRegistry() {
        bind(ForgeRegistries.SOUND_EVENTS, HexSounds::registerSounds)
        bind(ForgeRegistries.BLOCKS, HexBlocks::registerBlocks)
        bind(ForgeRegistries.ITEMS, HexBlocks::registerBlockItems)
        bind(ForgeRegistries.ITEMS, HexItems::registerItems)
    }

    fun initListeners() {
        // mod lifecycle
        val modBus = thedarkcolour.kotlinforforge.forge.MOD_BUS
        // game events
        val evBus = thedarkcolour.kotlinforforge.forge.FORGE_BUS

        modBus.addListener(this::commonSetup)

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
        evBus.register(CapSyncers::class.java)

        /*
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
         */
    }

    private fun <T : IForgeRegistryEntry<T>> bind(
        registry: IForgeRegistry<T>,
        source: Consumer<BiConsumer<T, ResourceLocation>>
    ) {
        FMLJavaModLoadingContext.get().modEventBus.addGenericListener(
            registry.registrySuperType
        ) { event: RegistryEvent.Register<T> ->
            val forgeRegistry = event.registry
            source.accept { t, rl ->
                t.registryName = rl
                forgeRegistry.register(t)
            }
        }
    }

    @SubscribeEvent
    fun commonSetup(evt: FMLCommonSetupEvent) {
        evt.enqueueWork {
            HexAdvancementTriggers.registerTriggers()
            ForgePacketHandler.init()
        }
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
