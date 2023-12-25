package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.eval.vm.ContinuationFrame;
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate;
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval;
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

/**
 * Stores the registry for continuation frame types, some utility methods, and all the types Hexcasting itself defines.
 */
@ParametersAreNonnullByDefault
public class HexContinuationTypes {
    public static final Registry<ContinuationFrame.Type<?>> REGISTRY = IXplatAbstractions.INSTANCE.getContinuationTypeRegistry();

    public static final String
            KEY_TYPE = HexAPI.MOD_ID + ":type",
            KEY_DATA = HexAPI.MOD_ID + ":data";

    public static void registerContinuations(BiConsumer<ContinuationFrame.Type<?>, ResourceLocation> r) {
        for (var e : CONTINUATIONS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, ContinuationFrame.Type<?>> CONTINUATIONS = new LinkedHashMap<>();

    public static final ContinuationFrame.Type<FrameEvaluate> EVALUATE = continuation("evaluate", FrameEvaluate.TYPE);
    public static final ContinuationFrame.Type<FrameForEach> FOREACH = continuation("foreach", FrameForEach.TYPE);
    public static final ContinuationFrame.Type<FrameFinishEval> END = continuation("end", FrameFinishEval.TYPE);

    private static <U extends ContinuationFrame, T extends ContinuationFrame.Type<U>> T continuation(String name, T continuation) {
        var old = CONTINUATIONS.put(modLoc(name), continuation);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return continuation;
    }
}
