package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// TODO: we REALLY need a cleanup of how sounds work. again.
public class HexEvalSounds {
    private static final IXplatRegister<EvalSound> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(HexRegistries.EVAL_SOUND);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<EvalSound> NOTHING = REGISTER.register("nothing", () ->
        new EvalSound(null, Integer.MIN_VALUE));
    public static final Supplier<EvalSound> NORMAL_EXECUTE = REGISTER.register("operator", () ->
        new EvalSound(HexSounds.CAST_NORMAL.value(), 0));
    public static final Supplier<EvalSound> SPELL = REGISTER.register("spell", () ->
        new EvalSound(HexSounds.CAST_SPELL.value(), 1000));
    public static final Supplier<EvalSound> HERMES = REGISTER.register("hermes", () ->
        new EvalSound(HexSounds.CAST_HERMES.value(), 2000));
    public static final Supplier<EvalSound> THOTH = REGISTER.register("thoth", () ->
        new EvalSound(HexSounds.CAST_THOTH.value(), 2500));

    public static final Supplier<EvalSound> MUTE = REGISTER.register("mute", () ->
        new EvalSound(null, 3000));

    public static final Supplier<EvalSound> MISHAP = REGISTER.register("mishap", () ->
        new EvalSound(HexSounds.CAST_FAILURE.value(), 4000));
}
