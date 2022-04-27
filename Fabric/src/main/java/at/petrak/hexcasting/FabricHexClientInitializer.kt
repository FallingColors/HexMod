import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import net.fabricmc.api.ClientModInitializer

object FabricHexClientInitializer : ClientModInitializer {
    override fun onInitializeClient() {
        FabricPacketHandler.initClient()
    }
}