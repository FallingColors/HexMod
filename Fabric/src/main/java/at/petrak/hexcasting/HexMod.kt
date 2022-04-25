import at.petrak.hexcasting.fabric.FabricHexConfig
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.fabricmc.api.ModInitializer

object HexMod : ModInitializer {
    override fun onInitialize() {
        IXplatAbstractions.INSTANCE.init()

        FabricHexConfig.setup()
    }
}