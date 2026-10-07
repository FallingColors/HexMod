package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.eval.vm.ImageComponentType;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Registry;

import java.util.function.Supplier;

/**
 * Stores the registry for casting image components, and all the component types Hexcasting itself defines.
 */
public class HexImageComponents {
    private static final IXplatRegister<ImageComponentType<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.IMAGE_COMPONENT);
    public static final Registry<ImageComponentType<?>> REGISTRY = IXplatAbstractions.INSTANCE.getImageComponentRegistry();

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<ImageComponentType<Iota>> RAVENMIND = REGISTER.register("ravenmind", () ->
            new ImageComponentType<>(IotaType.TYPED_CODEC, IotaType.TYPED_STREAM_CODEC, false));
}
