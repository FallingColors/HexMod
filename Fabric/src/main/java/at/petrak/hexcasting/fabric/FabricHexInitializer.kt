package at.petrak.hexcasting.fabric

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.addldata.ADMediaHolder
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.item.HexHolderItem
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.item.PigmentItem
import at.petrak.hexcasting.api.item.VariantItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.isOfTag
import at.petrak.hexcasting.common.blocks.behavior.HexComposting
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.casting.actions.spells.OpFlight
import at.petrak.hexcasting.common.casting.actions.spells.great.OpAltiora
import at.petrak.hexcasting.common.command.PatternResKeyArgument
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.items.ItemJewelerHammer
import at.petrak.hexcasting.common.items.magic.ItemMediaBattery
import at.petrak.hexcasting.common.items.storage.ItemScroll
import at.petrak.hexcasting.common.lib.*
import at.petrak.hexcasting.common.lib.hex.*
import at.petrak.hexcasting.common.misc.AkashicTreeGrower
import at.petrak.hexcasting.common.misc.BrainsweepingEvents
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder
import at.petrak.hexcasting.common.misc.RegisterMisc
import at.petrak.hexcasting.common.recipe.HexRecipeStuffRegistry
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.BrainsweepeeIngredients
import at.petrak.hexcasting.common.recipe.ingredient.state.StateIngredients
import at.petrak.hexcasting.fabric.cc.HexCardinalComponents
import at.petrak.hexcasting.fabric.cc.adimpl.CCHexHolder
import at.petrak.hexcasting.fabric.cc.adimpl.CCIotaHolder
import at.petrak.hexcasting.fabric.cc.adimpl.CCItemIotaHolder
import at.petrak.hexcasting.fabric.cc.adimpl.CCMediaHolder
import at.petrak.hexcasting.fabric.cc.adimpl.CCPigment
import at.petrak.hexcasting.fabric.cc.adimpl.CCVariantItem
import at.petrak.hexcasting.fabric.event.VillagerConversionCallback
import at.petrak.hexcasting.fabric.loot.FabricHexLootModJankery
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import at.petrak.hexcasting.fabric.recipe.FabricModConditionalIngredient
import at.petrak.hexcasting.fabric.recipe.FabricUnsealedIngredient
import at.petrak.hexcasting.fabric.storage.FabricImpetusStorage
import at.petrak.hexcasting.fabric.xplat.FabricXplatImpl
import at.petrak.hexcasting.interop.HexInterop
import at.petrak.hexcasting.xplat.IXplatAbstractions
import com.samsthenerd.inline.utils.EntityCradle
import com.samsthenerd.inline.utils.cradles.EntTypeCradle
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.EntityElytraEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.item.v1.DefaultItemComponentEvents
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.commands.synchronization.SingletonArgumentInfo
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator
import java.util.function.BiConsumer
import java.util.function.Function

object FabricHexInitializer : ModInitializer {
    lateinit var CONFIG: FabricHexConfig

    override fun onInitialize() {
        this.CONFIG = FabricHexConfig.setup()
        // workaround for Inline EntTypeCradles not being available on server, related to https://github.com/SamsTheNerd/inline/issues/34
        EntTypeCradle.fromTypeId(ResourceLocation.fromNamespaceAndPath("minecraft", "pig")).get().getType();
        FabricPacketHandler.initPackets()
        FabricPacketHandler.init()

        initListeners()

        initRegistries()

        ArgumentTypeRegistry.registerArgumentType(
            modLoc("pattern"),
            PatternResKeyArgument::class.java,
            SingletonArgumentInfo.contextFree { PatternResKeyArgument.id() }
        )
        HexAdvancementTriggers.registerTriggers(bind(BuiltInRegistries.TRIGGER_TYPES))
        HexComposting.setup()
        HexStrippables.init()
        FabricImpetusStorage.registerStorage()

        LootTableEvents.MODIFY.register { key, tableBuilder, source, lookup ->
            if (Blocks.AMETHYST_CLUSTER.lootTable.equals(key)) {
                tableBuilder.modifyPools { pool ->
                    pool.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 2.0f)))
                }
            }
        }

        HexInterop.init()
        RegisterMisc.register()
    }

    fun initListeners() {
        UseEntityCallback.EVENT.register(BrainsweepingEvents::interactWithBrainswept)
        VillagerConversionCallback.EVENT.register(BrainsweepingEvents::copyBrainsweepPostTransformation)
        AttackBlockCallback.EVENT.register { player, world, _, pos, _ ->
            // SUCCESS cancels further processing and, on the client, sends a packet to the server.
            // PASS falls back to further processing.
            // FAIL cancels further processing and does not send a packet to the server.
            if (ItemJewelerHammer.shouldFailToBreak(player, world.getBlockState(pos), pos)) {
                InteractionResult.SUCCESS // "success"
            } else {
                InteractionResult.PASS
            }
        }

        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            PatternRegistryManifest.processRegistry(server.overworld())
        }

        ServerTickEvents.END_WORLD_TICK.register(PlayerPositionRecorder::updateAllPlayers)
        ServerTickEvents.END_WORLD_TICK.register(OpFlight::tickAllPlayers)
        ServerTickEvents.END_WORLD_TICK.register(OpAltiora::checkAllPlayers)

        CommandRegistrationCallback.EVENT.register { dp, _, _ -> HexCommands.register(dp) }

        LootTableEvents.MODIFY.register { key, builder, _, _ ->
            FabricHexLootModJankery.lootLoad(key, builder::withPool)
        }

        EntityElytraEvents.CUSTOM.register { target, _ ->
            if (target is Player) {
                val altiora = IXplatAbstractions.INSTANCE.getAltiora(target)
                altiora != null
            } else {
                false
            }
        }

        ItemGroupEvents.modifyEntriesEvent(HexCreativeTabs.SCROLLS_KEY).register { r ->
            val keyList = ArrayList<ResourceKey<ActionRegistryEntry?>?>()
            val regi = FabricXplatImpl.INSTANCE.getActionRegistry()
            for (key in regi.registryKeySet()) if (isOfTag<ActionRegistryEntry?>(
                    regi,
                    key,
                    HexTags.Actions.PER_WORLD_PATTERN
                )
            ) keyList.add(key)
            keyList.sortWith(Comparator.comparing<ResourceKey<ActionRegistryEntry?>?, ResourceLocation?>(Function { obj: ResourceKey<ActionRegistryEntry?>? -> obj!!.location() }))
            for (key in keyList) {
                r.accept(
                    ItemScroll.withPerWorldPattern(
                        ItemStack(HexItems.SCROLL_LARGE),
                        key
                    )
                )
            }
        }
        ItemGroupEvents.modifyEntriesEvent(HexCreativeTabs.HEX_KEY).register { r ->
            for (item in this.itemsToAddToCreativeTab) {
                if (item is ItemMediaBattery) {
                    r.accept(HexItems.BATTERY_DUST_STACK.get())
                    r.accept(HexItems.BATTERY_SHARD_STACK.get())
                    r.accept(HexItems.BATTERY_CRYSTAL_STACK.get())
                    r.accept(HexItems.BATTERY_QUENCHED_SHARD_STACK.get())
                    r.accept(HexItems.BATTERY_QUENCHED_BLOCK_STACK.get())
                } else {
                    r.accept(item)
                }
            }
        }
    }

    private fun initRegistries() {
        HexBlockSetTypes.registerBlocks(BlockSetType::register)

        HexCreativeTabs.registerCreativeTabs(bind(BuiltInRegistries.CREATIVE_MODE_TAB))

        HexSounds.registerSounds(bind(BuiltInRegistries.SOUND_EVENT))
        HexBlocks.registerBlocks(bind(BuiltInRegistries.BLOCK))
        HexBlocks.registerBlockItems(boundForItem)
        HexBlockEntities.registerTiles(bind(BuiltInRegistries.BLOCK_ENTITY_TYPE))
        HexItems.registerItems(boundForItem)
        // Registry.register(IngredientDeserializer.REGISTRY, FabricModConditionalIngredient.ID, FabricModConditionalIngredient.Deserializer.INSTANCE)
        CustomIngredientSerializer.register(FabricUnsealedIngredient.Serializer.INSTANCE);
        CustomIngredientSerializer.register(FabricModConditionalIngredient.Serializer.INSTANCE);

        HexEntities.registerEntities(bind(BuiltInRegistries.ENTITY_TYPE))
        HexAttributes.register()
        FabricDefaultAttributeRegistry.register(EntityType.PLAYER,
            Player.createAttributes()
                .add(HexAttributes.GRID_ZOOM)
                .add(HexAttributes.SCRY_SIGHT)
                .add(HexAttributes.FEEBLE_MIND)
                .add(HexAttributes.AMBIT_RADIUS)
                .add(HexAttributes.MEDIA_CONSUMPTION_MODIFIER)
                .add(HexAttributes.SENTINEL_RADIUS))
        HexMobEffects.register()
        HexPotions.registerPotions(bind(BuiltInRegistries.POTION))
        HexDataComponents.registerDataComponents(bind(BuiltInRegistries.DATA_COMPONENT_TYPE))

        HexRecipeStuffRegistry.registerSerializers(bind(BuiltInRegistries.RECIPE_SERIALIZER))
        HexRecipeStuffRegistry.registerTypes(bind(BuiltInRegistries.RECIPE_TYPE))

        HexParticles.registerParticles(bind(BuiltInRegistries.PARTICLE_TYPE))

        HexLootFunctions.registerSerializers(bind(BuiltInRegistries.LOOT_FUNCTION_TYPE))

        HexIotaTypes.registerTypes(bind(IXplatAbstractions.INSTANCE.iotaTypeRegistry))
        HexActions.register(bind(IXplatAbstractions.INSTANCE.actionRegistry))
        HexSpecialHandlers.register(bind(IXplatAbstractions.INSTANCE.specialHandlerRegistry))
        HexArithmetics.register(bind(IXplatAbstractions.INSTANCE.arithmeticRegistry))
        HexContinuationTypes.registerContinuations(bind(IXplatAbstractions.INSTANCE.continuationTypeRegistry))
        HexEvalSounds.register(bind(IXplatAbstractions.INSTANCE.evalSoundRegistry))
        StateIngredients.register(bind(IXplatAbstractions.INSTANCE.stateIngredientRegistry))
        BrainsweepeeIngredients.register(bind(IXplatAbstractions.INSTANCE.brainsweepeeIngredientRegistry))


        // Because of Java's lazy-loading of classes, can't use Kotlin static initialization for
        // any calls that will eventually touch FeatureUtils.register(), as the growers here do,
        // unless the class is called in this initialization step.
        AkashicTreeGrower.init()

        // Done with soft implements in forge
        butYouCouldBeFire()

        HexStatistics.register()

        fabricOnlyRegistration()
    }

    // sorry lex (not sorry)
    private fun fabricOnlyRegistration() {
        for (item in BuiltInRegistries.ITEM) {
            if (item is PigmentItem) {
                HexCardinalComponents.PIGMENT_ITEM_LOOKUP.registerForItems({
                    item, _ -> CCPigment.ItemBased(item);
                }, item)
            }
            if (item is IotaHolderItem) {
                HexCardinalComponents.IOTA_HOLDER_LOOKUP.registerForItems({
                    item, _ -> CCItemIotaHolder.ItemBased(item);
                }, item)
            }
            if (item is HexHolderItem) {
                HexCardinalComponents.HEX_HOLDER_LOOKUP.registerForItems({
                    item, _ -> CCHexHolder.ItemBased(item);
                }, item)
            }
            if (item is VariantItem) {
                HexCardinalComponents.VARIANT_ITEM_LOOKUP.registerForItems({
                    item, _ -> CCVariantItem.ItemBased(item);
                }, item)
            }
        }

        HexCardinalComponents.MEDIA_HOLDER_LOOKUP.registerForItems({
         stack, _ -> CCMediaHolder.Static({ HexConfig.common().dustMediaAmount() }, ADMediaHolder.AMETHYST_DUST_PRIORITY, stack)
        }, HexItems.AMETHYST_DUST)

        HexCardinalComponents.MEDIA_HOLDER_LOOKUP.registerForItems({
            stack, _ -> CCMediaHolder.Static({ HexConfig.common().shardMediaAmount() }, ADMediaHolder.AMETHYST_SHARD_PRIORITY, stack)
        }, Items.AMETHYST_SHARD)

        HexCardinalComponents.MEDIA_HOLDER_LOOKUP.registerForItems({
                stack, _ -> CCMediaHolder.Static({ HexConfig.common().chargedCrystalMediaAmount() }, ADMediaHolder.CHARGED_AMETHYST_PRIORITY, stack)
        }, HexItems.CHARGED_AMETHYST)

        HexCardinalComponents.MEDIA_HOLDER_LOOKUP.registerForItems({
                stack, _ -> CCMediaHolder.Static({ MediaConstants.QUENCHED_SHARD_UNIT }, ADMediaHolder.QUENCHED_SHARD_PRIORITY, stack)
        }, HexItems.QUENCHED_SHARD)

        HexCardinalComponents.MEDIA_HOLDER_LOOKUP.registerForItems({
                stack, _ -> CCMediaHolder.Static({ MediaConstants.QUENCHED_BLOCK_UNIT }, ADMediaHolder.QUENCHED_ALLAY_PRIORITY, stack)
        }, HexBlocks.QUENCHED_ALLAY.asItem())

        HexCardinalComponents.IOTA_HOLDER_LOOKUP.registerForItems({
            stack, _ -> CCItemIotaHolder.Static(stack) {
            return@Static DoubleIota(Math.PI)
        }
        }, Items.PUMPKIN_PIE)
    }

    private val itemsToAddToCreativeTab : MutableSet<Item> = mutableSetOf()

	private val boundForItem : BiConsumer<Item, ResourceLocation> = BiConsumer {
        t, id -> this.itemsToAddToCreativeTab.add(t)
        Registry.register(BuiltInRegistries.ITEM, id, t)
    }


    private fun butYouCouldBeFire() {
        val flameOn = FlammableBlockRegistry.getDefaultInstance()
        for (log in listOf(
            HexBlocks.EDIFIED_LOG,
            HexBlocks.EDIFIED_LOG_AMETHYST,
            HexBlocks.EDIFIED_LOG_AVENTURINE,
            HexBlocks.EDIFIED_LOG_CITRINE,
            HexBlocks.EDIFIED_LOG_PURPLE,
            HexBlocks.STRIPPED_EDIFIED_LOG,
            HexBlocks.EDIFIED_WOOD,
            HexBlocks.STRIPPED_EDIFIED_LOG,
        )) {
            flameOn.add(log, 5, 5)
        }
        for (wood in listOf(
            HexBlocks.EDIFIED_PLANKS,
            HexBlocks.EDIFIED_PANEL,
            HexBlocks.EDIFIED_TILE,
            HexBlocks.EDIFIED_DOOR,
            HexBlocks.EDIFIED_TRAPDOOR,
            HexBlocks.EDIFIED_STAIRS,
            HexBlocks.EDIFIED_SLAB,
            HexBlocks.EDIFIED_FENCE,
            HexBlocks.EDIFIED_FENCE_GATE,
            HexBlocks.EDIFIED_SLAB,
            HexBlocks.EDIFIED_BUTTON,
            HexBlocks.EDIFIED_PRESSURE_PLATE,
        )) {
            flameOn.add(wood, 20, 5)
        }
        for (papery in listOf(
            HexBlocks.SCROLL_PAPER,
            HexBlocks.SCROLL_PAPER_LANTERN,
            HexBlocks.ANCIENT_SCROLL_PAPER,
            HexBlocks.ANCIENT_SCROLL_PAPER_LANTERN,

            )) {
            flameOn.add(papery, 100, 60)
        }
        for (leaves in listOf(
            HexBlocks.AMETHYST_EDIFIED_LEAVES,
            HexBlocks.AVENTURINE_EDIFIED_LEAVES,
            HexBlocks.CITRINE_EDIFIED_LEAVES,
        )) {
            flameOn.add(leaves, 60, 30)
        }
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}
