import at.petrak.hexcasting.fabric.FabricHexConfig
import at.petrak.hexcasting.fabric.network.FabricPacketHandler
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.fabricmc.api.ModInitializer

object FabricHexInitializer : ModInitializer {
    override fun onInitialize() {
        IXplatAbstractions.INSTANCE.init()
        FabricPacketHandler.init()
        FabricHexConfig.setup()
    }
}