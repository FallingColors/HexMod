package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexStatistics;
import at.petrak.hexcasting.common.blocks.behavior.HexComposting;
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables;
import at.petrak.hexcasting.common.casting.RegisterPatterns;
import at.petrak.hexcasting.common.casting.operators.spells.great.OpFlight;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.ItemJewelerHammer;
import at.petrak.hexcasting.common.items.ItemLens;
import at.petrak.hexcasting.common.lib.*;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.loot.HexLootHandler;
import at.petrak.hexcasting.common.misc.AkashicTreeGrower;
import at.petrak.hexcasting.common.misc.Brainsweeping;
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.forge.cap.CapSyncers;
import at.petrak.hexcasting.forge.cap.ForgeCapabilityHandler;
import at.petrak.hexcasting.forge.datagen.HexForgeDataGenerators;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.forge.interop.curios.CuriosRenderers;
import at.petrak.hexcasting.forge.network.ForgePacketHandler;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.forge.recipe.ForgeModConditionalIngredient;
import at.petrak.hexcasting.forge.recipe.ForgeUnsealedIngredient;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegisterEvent;
import thedarkcolour.kotlinforforge.KotlinModLoadingContext;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(HexAPI.MOD_ID)
public class ForgeHexInitializer {
    public ForgeHexInitializer() {
        initConfig();
        initRegistry();
        initListeners();
    }

    private static void initConfig() {
        var config = new ForgeConfigSpec.Builder().configure(ForgeHexConfig::new);
        var clientConfig = new ForgeConfigSpec.Builder().configure(ForgeHexConfig.Client::new);
        var serverConfig = new ForgeConfigSpec.Builder().configure(ForgeHexConfig.Server::new);
        HexConfig.setCommon(config.getLeft());
        HexConfig.setClient(clientConfig.getLeft());
        HexConfig.setServer(serverConfig.getLeft());
        var mlc = ModLoadingContext.get();
        mlc.registerConfig(ModConfig.Type.COMMON, config.getRight());
        mlc.registerConfig(ModConfig.Type.CLIENT, clientConfig.getRight());
        mlc.registerConfig(ModConfig.Type.SERVER, serverConfig.getRight());
    }

    private static void initRegistry() {
        bind(Registry.SOUND_EVENT_REGISTRY, HexSounds::registerSounds);
        bind(Registry.BLOCK_REGISTRY, HexBlocks::registerBlocks);
        bind(Registry.ITEM_REGISTRY, HexBlocks::registerBlockItems);
        bind(Registry.BLOCK_ENTITY_TYPE_REGISTRY, HexBlockEntities::registerTiles);
        bind(Registry.ITEM_REGISTRY, HexItems::registerItems);

        bind(Registry.RECIPE_SERIALIZER_REGISTRY, HexRecipeStuffRegistry::registerSerializers);
        bind(Registry.RECIPE_TYPE_REGISTRY, HexRecipeStuffRegistry::registerTypes);

        bind(Registry.ENTITY_TYPE_REGISTRY, HexEntities::registerEntities);

        bind(Registry.PARTICLE_TYPE_REGISTRY, HexParticles::registerParticles);

        ForgeHexArgumentTypeRegistry.ARGUMENT_TYPES.register(getModEventBus());

        HexIotaTypes.registerTypes();

        HexAdvancementTriggers.registerTriggers();
    }

    // https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/ForgeCommonInitializer.java
    private static <T> void bind(ResourceKey<Registry<T>> registry,
                                 Consumer<BiConsumer<T, ResourceLocation>> source) {
        getModEventBus().addListener((RegisterEvent event) -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    private static void initListeners() {
        var modBus = getModEventBus();
        var evBus = MinecraftForge.EVENT_BUS;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.register(ForgeHexClientInitializer.class));

        modBus.addListener((FMLCommonSetupEvent evt) ->
            evt.enqueueWork(() -> {
                ForgePacketHandler.init();
                HexComposting.setup();
                HexStrippables.init();
                RegisterPatterns.registerPatterns();
                // Forge does not strictly require TreeGrowers to initialize during early game stages, unlike Fabric
                // and Quilt.
                // However, all launcher panic if the same resource is registered twice.  But do need blocks and
                // items to be completely initialized.
                // Explicitly calling here avoids potential confusion, or reliance on tricks that may fail under
                // compiler optimization.
                AkashicTreeGrower.init();

                HexInterop.init();
            }));

        // We have to do these at some point when the registries are still open
        modBus.addListener((RegisterEvent evt) -> {
            if (evt.getRegistryKey().equals(Registry.ITEM_REGISTRY)) {
                CraftingHelper.register(ForgeUnsealedIngredient.ID, ForgeUnsealedIngredient.Serializer.INSTANCE);
                CraftingHelper.register(ForgeModConditionalIngredient.ID,
                    ForgeModConditionalIngredient.Serializer.INSTANCE);
                HexStatistics.register();
                HexLootFunctions.registerSerializers((lift, id) ->
                    Registry.register(Registry.LOOT_FUNCTION_TYPE, id, lift));
            }
        });

        modBus.addListener((FMLLoadCompleteEvent evt) ->
            HexAPI.LOGGER.info(PatternRegistry.getPatternCountInfo()));

        evBus.addListener((PlayerInteractEvent.EntityInteract evt) -> {
            var res = Brainsweeping.tradeWithVillager(
                evt.getEntity(), evt.getLevel(), evt.getHand(), evt.getTarget(), null);
            if (res.consumesAction()) {
                evt.setCanceled(true);
                evt.setCancellationResult(res);
            }
        });
        evBus.addListener((LivingConversionEvent.Post evt) ->
            Brainsweeping.copyBrainsweepFromVillager(evt.getEntity(), evt.getOutcome()));

        evBus.addListener((LivingEvent.LivingTickEvent evt) -> {
            OpFlight.INSTANCE.tickDownFlight(evt.getEntity());
            ItemLens.tickLens(evt.getEntity());
        });

        evBus.addListener((TickEvent.LevelTickEvent evt) -> {
            if (evt.phase == TickEvent.Phase.END && evt.level instanceof ServerLevel world) {
                PlayerPositionRecorder.updateAllPlayers(world);
            }
        });

        evBus.addListener((RegisterCommandsEvent evt) -> HexCommands.register(evt.getDispatcher()));

        evBus.addListener((PlayerEvent.BreakSpeed evt) -> {
            var pos = evt.getPosition();
            // tracing the dataflow, this is only empty if someone is calling a deprecated function for the
            // break speed. This will probably not ever hapen, but hey! i will never complain about correctness
            // enforced at the type level.
            if (pos.isEmpty()) {
                return;
            }
            evt.setCanceled(ItemJewelerHammer.shouldFailToBreak(evt.getEntity(), evt.getState(), pos.get()));
        });

        evBus.addListener((LootTableLoadEvent evt) -> HexLootHandler.lootLoad(
            evt.getName(),
            builder -> evt.getTable().addPool(builder.build())));

        // === Events implemented in other ways on Fabric

        // On Fabric this should be auto-synced
        evBus.addListener((PlayerEvent.StartTracking evt) -> {
            Entity target = evt.getTarget();
            if (evt.getTarget() instanceof ServerPlayer serverPlayer &&
                target instanceof Mob mob && IXplatAbstractions.INSTANCE.isBrainswept(mob)) {
                ForgePacketHandler.getNetwork()
                    .send(PacketDistributor.PLAYER.with(() -> serverPlayer), MsgBrainsweepAck.of(mob));
            }
        });

        // Implemented with a mixin on Farbc
        evBus.addListener((BlockEvent.BlockToolModificationEvent evt) -> {
            if (!evt.isSimulated() && evt.getToolAction() == ToolActions.AXE_STRIP) {
                BlockState bs = evt.getState();
                var output = HexStrippables.STRIPPABLES.get(bs.getBlock());
                if (output != null) {
                    evt.setFinalState(output.withPropertiesOf(bs));
                }
            }
        });

        // Caps are cardinal components on farbc
        modBus.addListener(ForgeCapabilityHandler::registerCaps);
        evBus.addGenericListener(ItemStack.class, ForgeCapabilityHandler::attachItemCaps);
        evBus.addGenericListener(BlockEntity.class, ForgeCapabilityHandler::attachBlockEntityCaps);
        evBus.addGenericListener(Entity.class, ForgeCapabilityHandler::attachEntityCaps);

        modBus.register(HexForgeDataGenerators.class);
        modBus.register(ForgeCapabilityHandler.class);
        evBus.register(CapSyncers.class);

        if (ModList.get().isLoaded(HexInterop.Forge.CURIOS_API_ID)) {
            modBus.addListener(CuriosApiInterop::onInterModEnqueue);
            modBus.addListener(CuriosApiInterop::onClientSetup);
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modBus.addListener(CuriosRenderers::onLayerRegister));
        }
    }

    // aaaauughhg
    private static IEventBus getModEventBus() {
        return KotlinModLoadingContext.Companion.get().getKEventBus();
    }
}
