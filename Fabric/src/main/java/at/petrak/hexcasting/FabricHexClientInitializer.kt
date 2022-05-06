import at.petrak.hexcasting.client.HexAdditionalRenderers
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object FabricHexClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        FabricPacketHandler.initClient()

        WorldRenderEvents.BEFORE_DEBUG_RENDER.register { ctx ->
            HexAdditionalRenderers.overlayLevel(ctx.matrixStack(), ctx.tickDelta())
        }
        HudRenderCallback.EVENT.register(HexAdditionalRenderers::overlayGui)
    }
}