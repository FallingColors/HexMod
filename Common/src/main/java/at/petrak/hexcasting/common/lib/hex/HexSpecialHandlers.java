package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.common.casting.actions.eval.SpecialHandlerForEach;
import at.petrak.hexcasting.common.casting.actions.math.SpecialHandlerNumberLiteral;
import at.petrak.hexcasting.common.casting.actions.stack.SpecialHandlerMask;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;

import java.util.function.Supplier;

public class HexSpecialHandlers {
    private static final IXplatRegistrar<SpecialHandler.Factory<?>> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(HexRegistries.SPECIAL_HANDLER);

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Supplier<SpecialHandler.Factory<SpecialHandlerNumberLiteral>> NUMBER = REGISTRAR.register("number",
        SpecialHandlerNumberLiteral.Factory::new);
    public static final Supplier<SpecialHandler.Factory<SpecialHandlerMask>> MASK = REGISTRAR.register("mask",
        SpecialHandlerMask.Factory::new);
    public static final Supplier<SpecialHandler.Factory<SpecialHandlerForEach>> FOR_EACH = REGISTRAR.register("for_each",
        SpecialHandlerForEach.Factory::new);
}
