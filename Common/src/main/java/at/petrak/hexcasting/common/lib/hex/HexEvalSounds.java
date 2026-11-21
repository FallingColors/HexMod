package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.common.lib.HexRegistries;
import at.petrak.hexcasting.common.lib.HexSounds;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;

import java.util.function.Supplier;

// TODO: we REALLY need a cleanup of how sounds work. again.
public class HexEvalSounds {
    private static final IXplatRegistrar<EvalSound> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(HexRegistries.EVAL_SOUND);

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Supplier<EvalSound> NOTHING = REGISTRAR.register("nothing", () ->
        new EvalSound(null, Integer.MIN_VALUE));
    public static final Supplier<EvalSound> NORMAL_EXECUTE = REGISTRAR.register("operator", () ->
        new EvalSound(HexSounds.CAST_NORMAL.value(), 0));
    public static final Supplier<EvalSound> SPELL = REGISTRAR.register("spell", () ->
        new EvalSound(HexSounds.CAST_SPELL.value(), 1000));
    public static final Supplier<EvalSound> HERMES = REGISTRAR.register("hermes", () ->
        new EvalSound(HexSounds.CAST_HERMES.value(), 2000));
    public static final Supplier<EvalSound> THOTH = REGISTRAR.register("thoth", () ->
        new EvalSound(HexSounds.CAST_THOTH.value(), 2500));

    public static final Supplier<EvalSound> MUTE = REGISTRAR.register("mute", () ->
        new EvalSound(null, 3000));

    public static final Supplier<EvalSound> MISHAP = REGISTRAR.register("mishap", () ->
        new EvalSound(HexSounds.CAST_FAILURE.value(), 4000));
}
