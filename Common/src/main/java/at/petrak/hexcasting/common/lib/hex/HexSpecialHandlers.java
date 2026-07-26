package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.castables.SpecialHandler;
import at.petrak.hexcasting.common.casting.actions.eval.SpecialHandlerForEach;
import at.petrak.hexcasting.common.casting.actions.math.SpecialHandlerNumberLiteral;
import at.petrak.hexcasting.common.casting.actions.stack.SpecialHandlerMask;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexSpecialHandlers {
    private static final IXplatRegister<SpecialHandler.Factory<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.SPECIAL_HANDLER);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<SpecialHandler.Factory<SpecialHandlerNumberLiteral>> NUMBER = REGISTER.register("number",
        SpecialHandlerNumberLiteral.Factory::new);
    public static final Supplier<SpecialHandler.Factory<SpecialHandlerMask>> MASK = REGISTER.register("mask",
        SpecialHandlerMask.Factory::new);
    public static final Supplier<SpecialHandler.Factory<SpecialHandlerForEach>> FOR_EACH = REGISTER.register("for_each",
        SpecialHandlerForEach.Factory::new);
}
