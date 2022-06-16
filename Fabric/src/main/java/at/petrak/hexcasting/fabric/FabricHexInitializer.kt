package at.petrak.hexcasting.fabric

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.api.mod.HexStatistics
import at.petrak.hexcasting.common.blocks.behavior.HexComposting
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.command.PatternResLocArgument
import at.petrak.hexcasting.common.entities.HexEntities
import at.petrak.hexcasting.common.items.ItemJewelerHammer
import at.petrak.hexcasting.common.lib.*
import at.petrak.hexcasting.common.loot.HexLootHandler
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.common.misc.PlayerPositionRecorder
import at.petrak.hexcasting.common.recipe.HexRecipeSerializers
import at.petrak.hexcasting.fabric.event.VillagerConversionCallback
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import at.petrak.hexcasting.fabric.recipe.FabricUnsealedIngredient
import at.petrak.hexcasting.fabric.storage.FabricImpetusStorage
import at.petrak.hexcasting.interop.HexInterop
import io.github.tropheusj.serialization_hooks.ingredient.IngredientDeserializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.commands.synchronization.ArgumentTypes
import net.minecraft.commands.synchronization.EmptyArgumentSerializer
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import java.util.function.BiConsumer

object FabricHexInitializer : ModInitializer {
    override fun onInitialize() {
        FabricHexConfig.setup()
        FabricPacketHandler.init()

        initListeners()

        initRegistries()

        ArgumentTypes.register(
            "hexcasting:pattern",
            PatternResLocArgument::class.java,
            EmptyArgumentSerializer { PatternResLocArgument.id() }
        )
        RegisterPatterns.registerPatterns()
        HexAdvancementTriggers.registerTriggers()
        HexComposting.setup()
        HexStrippables.init()
        FabricImpetusStorage.registerStorage()

        HexInterop.init()
    }

    fun initListeners() {
        UseEntityCallback.EVENT.register(Brainsweeping::tradeWithVillager)
        VillagerConversionCallback.EVENT.register(Brainsweeping::copyBrainsweepFromVillager)
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

        ServerTickEvents.END_WORLD_TICK.register(PlayerPositionRecorder::updateAllPlayers)

        CommandRegistrationCallback.EVENT.register { dp, _ -> HexCommands.register(dp) }

        LootTableLoadingCallback.EVENT.register { _, _, id, supplier, _ ->
            HexLootHandler.lootLoad(
                id,
            ) { supplier.withPool(it) }
        }
    }

    fun initRegistries() {
        HexSounds.registerSounds(bind(Registry.SOUND_EVENT))
        HexBlocks.registerBlocks(bind(Registry.BLOCK))
        HexBlocks.registerBlockItems(bind(Registry.ITEM))
        HexBlockEntities.registerTiles(bind(Registry.BLOCK_ENTITY_TYPE))
        HexItems.registerItems(bind(Registry.ITEM))
        Registry.register(
            IngredientDeserializer.REGISTRY,
            modLoc("unsealed"),
            FabricUnsealedIngredient.Deserializer.INSTANCE
        )

        HexEntities.registerEntities(bind(Registry.ENTITY_TYPE))

        HexRecipeSerializers.registerSerializers(bind(Registry.RECIPE_SERIALIZER))
        HexParticles.registerParticles(bind(Registry.PARTICLE_TYPE))

        HexLootFunctions.registerSerializers(bind(Registry.LOOT_FUNCTION_TYPE))

        HexIotaTypes.registerTypes()


        // Done with soft implements in forge
        val flameOn = FlammableBlockRegistry.getDefaultInstance()
        for (log in listOf(
            HexBlocks.EDIFIED_LOG,
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
            HexBlocks.EDIFIED_STAIRS,
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

        HexRecipeSerializers.registerTypes()
        HexStatistics.register()
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}
