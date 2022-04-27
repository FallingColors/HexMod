package at.petrak.hexcasting.api;

import com.google.common.base.Suppliers;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public interface HexAPI {
    String MOD_ID = "hexcasting";
    Logger LOGGER = LogManager.getLogger(MOD_ID);

    Supplier<HexAPI> INSTANCE = Suppliers.memoize(() -> {
        try {
            return (HexAPI) Class.forName("at.petrak.hexcasting.common.impl.HexAPIImpl")
                .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            LogManager.getLogger().warn("Unable to find HexAPIImpl, using a dummy");
            return new HexAPI() {
            };
        }
    });

    static HexAPI instance() {
        return INSTANCE.get();
    }

    static ResourceLocation modLoc(String s) {
        return new ResourceLocation(MOD_ID, s);
    }
}
