package at.petrak.hexcasting.fabric

import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.client.HexAdditionalRenderers
import at.petrak.hexcasting.client.RegisterClientStuff
import at.petrak.hexcasting.client.ShiftScrollListener
import at.petrak.hexcasting.fabric.event.MouseScrollCallback
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import at.petrak.hexcasting.interop.HexInterop
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

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

        MouseScrollCallback.EVENT.register(ShiftScrollListener::onScrollInGameplay)

        RegisterClientStuff.init()
        RegisterClientStuff.registerParticles()
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
    }
}
