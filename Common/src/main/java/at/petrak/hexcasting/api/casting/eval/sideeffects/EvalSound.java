package at.petrak.hexcasting.api.casting.eval.sideeffects;

import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

/**
 * The kind of sound that plays after a cast.
 *
 * @param sound    the actual sound file
 * @param priority the priority of this sound. the sound with the highest priority in a given cast will be
 *                 played.
 *                 shortcutMetacasting takes precedence over this.
 */
public record EvalSound(@Nullable SoundEvent sound, int priority) {
    public EvalSound greaterOf(EvalSound that) {
        return (this.priority > that.priority) ? this : that;
    }
}
