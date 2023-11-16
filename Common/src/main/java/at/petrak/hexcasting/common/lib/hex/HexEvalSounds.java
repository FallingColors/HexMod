package at.petrak.hexcasting.common.lib.hex;

import at.petrak.hexcasting.api.casting.eval.sideeffects.EvalSound;
import at.petrak.hexcasting.common.lib.HexSounds;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

// TODO: we REALLY need a cleanup of how sounds work. again.
public class HexEvalSounds {
    private static final Map<ResourceLocation, EvalSound> SOUNDS = new LinkedHashMap<>();

    public static final EvalSound NOTHING = make("nothing",
        new EvalSound(null, Integer.MIN_VALUE));
    public static final EvalSound NORMAL_EXECUTE = make("operator",
        new EvalSound(HexSounds.CAST_NORMAL, 0));
    public static final EvalSound SPELL = make("spell",
        new EvalSound(HexSounds.CAST_SPELL, 1000));
    public static final EvalSound HERMES = make("hermes",
        new EvalSound(HexSounds.CAST_HERMES, 2000));
    public static final EvalSound THOTH = make("thoth",
        new EvalSound(HexSounds.CAST_THOTH, 2500));

    public static final EvalSound MUTE = make("mute",
        new EvalSound(null, 3000));

    public static final EvalSound MISHAP = make("mishap",
        new EvalSound(HexSounds.CAST_FAILURE, 4000));

    private static EvalSound make(String name, EvalSound sound) {
        var old = SOUNDS.put(modLoc(name), sound);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return sound;
    }

    public static void register(BiConsumer<EvalSound, ResourceLocation> r) {
        for (var e : SOUNDS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }
}
