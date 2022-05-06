import at.petrak.hexcasting.api.advancements.HexAdvancementTriggers
import at.petrak.hexcasting.common.blocks.behavior.HexComposting
import at.petrak.hexcasting.common.blocks.behavior.HexStrippables
import at.petrak.hexcasting.common.casting.RegisterPatterns
import at.petrak.hexcasting.common.command.PatternResLocArgument
import at.petrak.hexcasting.common.lib.*
import at.petrak.hexcasting.common.misc.Brainsweeping
import at.petrak.hexcasting.fabric.FabricHexConfig
import at.petrak.hexcasting.fabric.event.VillagerConversionCallback
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.commands.synchronization.ArgumentTypes
import net.minecraft.commands.synchronization.EmptyArgumentSerializer
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
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
    }

    fun initListeners() {
        UseEntityCallback.EVENT.register(Brainsweeping::tradeWithVillager)
        VillagerConversionCallback.EVENT.register(Brainsweeping::copyBrainsweepFromVillager)
    }

    fun initRegistries() {
        HexSounds.registerSounds(bind(Registry.SOUND_EVENT))
        HexBlocks.registerBlocks(bind(Registry.BLOCK))
        HexBlocks.registerBlockItems(bind(Registry.ITEM))
        HexItems.registerItems(bind(Registry.ITEM))
        HexBlockEntities.registerTiles(bind(Registry.BLOCK_ENTITY_TYPE))

        HexParticles.registerParticles(bind(Registry.PARTICLE_TYPE))
    }

    private fun <T> bind(registry: Registry<in T>): BiConsumer<T, ResourceLocation> =
        BiConsumer<T, ResourceLocation> { t, id -> Registry.register(registry, id, t) }
}