package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.SpecialHandler;
import at.petrak.hexcasting.common.casting.operators.math.SpecialHandlerNumberLiteral;
import at.petrak.hexcasting.common.casting.operators.stack.SpecialHandlerMask;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexSpecialHandlers {
    public static final Registry<SpecialHandler.Factory<?>> REGISTRY =
        IXplatAbstractions.INSTANCE.getSpecialHandlerRegistry();

    private static final Map<ResourceLocation, SpecialHandler.Factory<?>> SPECIAL_HANDLERS = new LinkedHashMap<>();

    public static final SpecialHandler.Factory<SpecialHandlerNumberLiteral> NUMBER = make("number",
        new SpecialHandlerNumberLiteral.Factory());
    public static final SpecialHandler.Factory<SpecialHandlerMask> MASK = make("mask",
        new SpecialHandlerMask.Factory());

    private static <T extends SpecialHandler> SpecialHandler.Factory<T> make(String name,
        SpecialHandler.Factory<T> handler) {
        var old = SPECIAL_HANDLERS.put(modLoc(name), handler);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return handler;
    }

    public static void register(BiConsumer<SpecialHandler.Factory<?>, ResourceLocation> r) {
        for (var e : SPECIAL_HANDLERS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }
}
