import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.fabricmc.api.ModInitializer

object HexMod : ModInitializer {
    override fun onInitialize() {
        IXplatAbstractions.INSTANCE.init()
    }
}