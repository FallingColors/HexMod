package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate;
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval;
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for continuation frame types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexContinuationTypes {
    private static final IXplatRegister<ContinuationFrame.Type<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.CONTINUATION_TYPE);

    public static final Registry<ContinuationFrame.Type<?>> REGISTRY = IXplatAbstractions.INSTANCE.getContinuationTypeRegistry();

    public static void register() {
        REGISTER.registerAll();
    }

    public static final String
            KEY_TYPE = HexAPI.MOD_ID + ":type",
            KEY_DATA = HexAPI.MOD_ID + ":data";

    public static final Supplier<ContinuationFrame.Type<FrameEvaluate>> EVALUATE = REGISTER.register("evaluate", () -> FrameEvaluate.TYPE);
    public static final Supplier<ContinuationFrame.Type<FrameForEach>> FOREACH = REGISTER.register("foreach", () -> FrameForEach.TYPE);
    public static final Supplier<ContinuationFrame.Type<FrameFinishEval>> END = REGISTER.register("end", () -> FrameFinishEval.TYPE);
}
