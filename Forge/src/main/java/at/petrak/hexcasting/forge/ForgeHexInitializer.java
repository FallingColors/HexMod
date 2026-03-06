package at.petrak.hexcasting.forge;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers;
import at.petrak.hexcasting.api.mod.HexConfig;
import at.petrak.hexcasting.api.mod.HexStatistics;
import at.petrak.hexcasting.common.blocks.behavior.HexComposting;
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables;
import at.petrak.hexcasting.common.casting.PatternRegistryManifest;
import at.petrak.hexcasting.common.casting.actions.spells.OpFlight;
import at.petrak.hexcasting.common.casting.actions.spells.great.OpAltiora;
import at.petrak.hexcasting.common.entities.HexEntities;
import at.petrak.hexcasting.common.items.ItemJewelerHammer;
import at.petrak.hexcasting.common.lib.*;
import at.petrak.hexcasting.common.lib.hex.*;
import at.petrak.hexcasting.common.misc.AkashicTreeGrower;
import at.petrak.hexcasting.common.misc.BrainsweepingEvents;
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder;
import at.petrak.hexcasting.common.misc.RegisterMisc;
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry;
import at.petrak.hexcasting.forge.cap.CapSyncers;
import at.petrak.hexcasting.forge.cap.ForgeCapabilityHandler;
import at.petrak.hexcasting.forge.cap.adimpl.CapClientCastingStack;
import at.petrak.hexcasting.forge.interop.curios.CuriosApiInterop;
import at.petrak.hexcasting.forge.interop.curios.CuriosRenderers;
import at.petrak.hexcasting.forge.lib.ForgeHexArgumentTypeRegistry;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import at.petrak.hexcasting.forge.network.MsgBrainsweepAck;
import at.petrak.hexcasting.forge.lib.ForgeHexIngredientTypes;
import at.petrak.hexcasting.interop.HexInterop;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.RegisterEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Mod(HexAPI.MOD_ID)
public class ForgeHexInitializer {
    private static IEventBus MOD_BUS;

    public ForgeHexInitializer(IEventBus modBus) {
        MOD_BUS = modBus;
        initConfig();
        initRegistries();
        initRegistry();
        initListeners(modBus);
    }

    private static void initConfig() {
        var config = new ModConfigSpec.Builder().configure(ForgeHexConfig::new);
        var clientConfig = new ModConfigSpec.Builder().configure(ForgeHexConfig.Client::new);
        var serverConfig = new ModConfigSpec.Builder().configure(ForgeHexConfig.Server::new);
        HexConfig.setCommon(config.getLeft());
        HexConfig.setClient(clientConfig.getLeft());
        HexConfig.setServer(serverConfig.getLeft());
        var modContainer = ModList.get().getModContainerById(HexAPI.MOD_ID).orElseThrow();
        modContainer.registerConfig(ModConfig.Type.COMMON, config.getRight());
        modContainer.registerConfig(ModConfig.Type.CLIENT, clientConfig.getRight());
        modContainer.registerConfig(ModConfig.Type.SERVER, serverConfig.getRight());
    }

    public static void initRegistries() {
        if (!(BuiltInRegistries.REGISTRY instanceof MappedRegistry<?> rootRegistry)) return;
        rootRegistry.unfreeze();

        IXplatAbstractions.INSTANCE.getActionRegistry();
        IXplatAbstractions.INSTANCE.getSpecialHandlerRegistry();
        IXplatAbstractions.INSTANCE.getIotaTypeRegistry();
        IXplatAbstractions.INSTANCE.getArithmeticRegistry();
        IXplatAbstractions.INSTANCE.getContinuationTypeRegistry();
        IXplatAbstractions.INSTANCE.getEvalSoundRegistry();

        rootRegistry.freeze();
    }

    private static void initRegistry() {
        bind(Registries.SOUND_EVENT, HexSounds::registerSounds);

        HexBlockSetTypes.registerBlocks(BlockSetType::register);

        bind(Registries.CREATIVE_MODE_TAB, HexCreativeTabs::registerCreativeTabs);

        bind(Registries.BLOCK, HexBlocks::registerBlocks);
        bind(Registries.ITEM, HexBlocks::registerBlockItems);
        bind(Registries.BLOCK_ENTITY_TYPE, HexBlockEntities::registerTiles);
        bind(Registries.ITEM, HexItems::registerItems);

        bind(Registries.RECIPE_SERIALIZER, HexRecipeStuffRegistry::registerSerializers);
        bind(Registries.RECIPE_TYPE, HexRecipeStuffRegistry::registerTypes);

        bind(Registries.ENTITY_TYPE, HexEntities::registerEntities);
        bind(Registries.ATTRIBUTE, HexAttributes::register);
        bind(Registries.MOB_EFFECT, HexMobEffects::register);
        bind(Registries.POTION, HexPotions::register);

        bind(Registries.PARTICLE_TYPE, HexParticles::registerParticles);

        bind(Registries.DATA_COMPONENT_TYPE, reg -> reg.accept(HexDataComponents.STACK_DATA, HexAPI.modLoc("stack_data")));

        bind(HexRegistries.IOTA_TYPE, HexIotaTypes::registerTypes);
        bind(HexRegistries.ACTION, HexActions::register);
        bind(HexRegistries.SPECIAL_HANDLER, HexSpecialHandlers::register);
        bind(HexRegistries.ARITHMETIC, HexArithmetics::register);
        bind(HexRegistries.CONTINUATION_TYPE, HexContinuationTypes::registerContinuations);
        bind(HexRegistries.EVAL_SOUND, HexEvalSounds::register);

        ForgeHexArgumentTypeRegistry.ARGUMENT_TYPES.register(MOD_BUS);
        ForgeHexLootMods.REGISTRY.register(MOD_BUS);

        // Advancement triggers must be registered via RegisterEvent (registry is frozen during mod init)
        bind(Registries.TRIGGER_TYPE, reg -> {
            reg.accept(HexAdvancementTriggers.OVERCAST_TRIGGER, HexAPI.modLoc("overcast"));
            reg.accept(HexAdvancementTriggers.SPEND_MEDIA_TRIGGER, HexAPI.modLoc("spend_media"));
            reg.accept(HexAdvancementTriggers.FAIL_GREAT_SPELL_TRIGGER, HexAPI.modLoc("fail_to_cast_great_spell"));
        });

        RegisterMisc.register();
    }

    // https://github.com/VazkiiMods/Botania/blob/1.18.x/Forge/src/main/java/vazkii/botania/forge/ForgeCommonInitializer.java
    private static <T> void bind(ResourceKey<? extends Registry<T>> registry,
        Consumer<BiConsumer<T, ResourceLocation>> source) {
        MOD_BUS.addListener((RegisterEvent event) -> {
            if (registry.equals(event.getRegistryKey())) {
                source.accept((t, rl) -> event.register(registry, rl, () -> t));
            }
        });
    }

    private static void initListeners(IEventBus modBus) {
        var evBus = NeoForge.EVENT_BUS;

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.register(ForgeHexClientInitializer.class); // client init, colors, particles, etc.
        }

        modBus.addListener((FMLCommonSetupEvent evt) ->
            evt.enqueueWork(() -> {
                HexComposting.setup();
                HexStrippables.init();
                // Forge does not strictly require TreeGrowers to initialize during early game stages, unlike Fabric
                // and Quilt.
                // However, all launcher panic if the same resource is registered twice.  But do need blocks and
                // items to be completely initialized.
                // Explicitly calling here avoids potential confusion, or reliance on tricks that may fail under
                // compiler optimization.
                AkashicTreeGrower.init();

                HexInterop.init();
            }));

        modBus.addListener((BuildCreativeModeTabContentsEvent evt) -> {
            HexBlocks.registerBlockCreativeTab(evt::accept, evt.getTab());
            HexItems.registerItemCreativeTab(evt, evt.getTab());
        });

        // RegisterBrewingRecipesEvent is on NeoForge bus, not mod bus
        evBus.addListener((RegisterBrewingRecipesEvent evt) ->
            HexPotions.addRecipes(evt.getBuilder()));


        ForgeHexIngredientTypes.INGREDIENT_TYPES.register(modBus);

        // We have to do these at some point when the registries are still open
        modBus.addListener((RegisterEvent evt) -> {
            if (evt.getRegistryKey().equals(Registries.ITEM)) {
                HexStatistics.register();
                HexLootFunctions.registerSerializers((lift, id) ->
                    Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE, id, lift));
            }
        });

        evBus.register(CapClientCastingStack.class);

        evBus.addListener((PlayerInteractEvent.EntityInteract evt) -> {
            var res = BrainsweepingEvents.interactWithBrainswept(
                evt.getEntity(), evt.getLevel(), evt.getHand(), evt.getTarget(), null);
            if (res.consumesAction()) {
                evt.setCanceled(true);
                evt.setCancellationResult(res);
            }
        });
        evBus.addListener((LivingConversionEvent.Post evt) ->
            BrainsweepingEvents.copyBrainsweepPostTransformation(evt.getEntity(), evt.getOutcome()));

        evBus.addListener((EntityTickEvent.Post evt) -> {
            if (evt.getEntity() instanceof ServerPlayer splayer) {
                OpFlight.tickDownFlight(splayer);
                OpAltiora.checkPlayerCollision(splayer);
            }
        });

        evBus.addListener((LevelTickEvent.Post evt) -> {
            if (evt.getLevel() instanceof ServerLevel world) {
                PlayerPositionRecorder.updateAllPlayers(world);
            }
        });

        evBus.addListener((ServerStartedEvent evt) ->
            PatternRegistryManifest.processRegistry(evt.getServer().overworld()));

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

        // === Events implemented in other ways on Fabric

        // On Fabric this should be auto-synced
        evBus.addListener((PlayerEvent.StartTracking evt) -> {
            Entity target = evt.getTarget();
            if (evt.getTarget() instanceof ServerPlayer serverPlayer &&
                target instanceof Mob mob && IXplatAbstractions.INSTANCE.isBrainswept(mob)) {
                PacketDistributor.sendToPlayer(serverPlayer, MsgBrainsweepAck.of(mob));
            }
        });

        // Implemented with a mixin on Farbc
        evBus.addListener((BlockEvent.BlockToolModificationEvent evt) -> {
            if (!evt.isSimulated() && evt.getItemAbility() == ItemAbilities.AXE_STRIP) {
                BlockState bs = evt.getState();
                var output = HexStrippables.STRIPPABLES.get(bs.getBlock());
                if (output != null) {
                    evt.setFinalState(output.withPropertiesOf(bs));
                }
            }
        });

        // Capabilities - register providers via RegisterCapabilitiesEvent (replaces AttachCapabilitiesEvent)
        modBus.addListener(ForgeCapabilityHandler::registerCapabilities);

        try {
            modBus.register(Class.forName("at.petrak.hexcasting.forge.datagen.ForgeHexDataGenerators"));
        } catch (ClassNotFoundException ignored) {
            // Datagen package excluded from build
        }
        // ForgeCapabilityHandler uses addListener(registerCapabilities), no @SubscribeEvent
        evBus.register(CapSyncers.class);

        modBus.addListener((EntityAttributeModificationEvent e) -> {
            var attrs = BuiltInRegistries.ATTRIBUTE;
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.GRID_ZOOM_KEY));
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.SCRY_SIGHT_KEY));
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.FEEBLE_MIND_KEY));
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.MEDIA_CONSUMPTION_MODIFIER_KEY));
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.AMBIT_RADIUS_KEY));
            e.add(EntityType.PLAYER, attrs.getHolderOrThrow(HexAttributes.SENTINEL_RADIUS_KEY));
        });

        if (ModList.get().isLoaded(HexInterop.Forge.CURIOS_API_ID)) {
            modBus.addListener(CuriosApiInterop::onInterModEnqueue);
            modBus.addListener(CuriosApiInterop::onClientSetup);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                modBus.addListener(CuriosRenderers::onLayerRegister);
            }
        }
    }
}
