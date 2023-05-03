package at.petrak.hexcasting.fabric

import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.RegisterClientStuff
import at.petrak.hexcasting.client.ShiftScrollListener
import at.petrak.hexcasting.client.gui.PatternTooltipComponent
import at.petrak.hexcasting.client.model.HexModelLayers
import at.petrak.hexcasting.client.render.HexAdditionalRenderers
import at.petrak.hexcasting.common.casting.PatternRegistryManifest
import at.petrak.hexcasting.common.lib.HexParticles
import at.petrak.hexcasting.fabric.event.MouseScrollCallback
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import at.petrak.hexcasting.interop.HexInterop
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.fabricmc.fabric.api.client.rendering.v1.*
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.Function

object FabricHexClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        FabricPacketHandler.initClient()

        WorldRenderEvents.AFTER_TRANSLUCENT.register { ctx ->
            HexAdditionalRenderers.overlayLevel(ctx.matrixStack(), ctx.tickDelta())
        }
        HudRenderCallback.EVENT.register(HexAdditionalRenderers::overlayGui)
        WorldRenderEvents.START.register { ClientTickCounter.renderTickStart(it.tickDelta()) }
        ClientTickEvents.END_CLIENT_TICK.register {
            ClientTickCounter.clientTickEnd()
            ShiftScrollListener.clientTickEnd()
        }
        TooltipComponentCallback.EVENT.register(PatternTooltipComponent::tryConvert)
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            PatternRegistryManifest.processRegistry(null)
        }

        MouseScrollCallback.EVENT.register(ShiftScrollListener::onScrollInGameplay)

        RegisterClientStuff.init()
        HexModelLayers.init { loc, defn -> EntityModelLayerRegistry.registerModelLayer(loc, defn::get) }


        HexParticles.FactoryHandler.registerFactories(object : HexParticles.FactoryHandler.Consumer {
            override fun <T : ParticleOptions?> register(type: ParticleType<T>, constructor: Function<SpriteSet, ParticleProvider<T>>) {
                ParticleFactoryRegistry.getInstance().register(type, constructor::apply)
            }
        })

        // how ergonomic
        RegisterClientStuff.registerBlockEntityRenderers(object :
            RegisterClientStuff.BlockEntityRendererRegisterererer {
            override fun <T : BlockEntity> registerBlockEntityRenderer(
                type: BlockEntityType<T>,
                berp: BlockEntityRendererProvider<in T>
            ) {
                BlockEntityRendererRegistry.register(type, berp)
            }
        })

        HexInterop.clientInit()
        RegisterClientStuff.registerColorProviders(
            { colorizer, item -> ColorProviderRegistry.ITEM.register(colorizer, item) },
            { colorizer, block -> ColorProviderRegistry.BLOCK.register(colorizer, block) })
        ModelLoadingRegistry.INSTANCE.registerModelProvider(RegisterClientStuff::onModelRegister)
    }
}
