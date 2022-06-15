package at.petrak.hexcasting.common.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexSounds {
    public static void registerSounds(BiConsumer<SoundEvent, ResourceLocation> r) {
        for (var e : SOUNDS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, SoundEvent> SOUNDS = new LinkedHashMap<>();

    public static final SoundEvent ADD_LINE = sound("casting.add_line");
    public static final SoundEvent START_PATTERN = sound("casting.start_pattern");
    public static final SoundEvent ADD_PATTERN = sound("casting.add_pattern");
    public static final SoundEvent FAIL_PATTERN = sound("casting.fail_pattern");
    public static final SoundEvent CASTING_AMBIANCE = sound("casting.ambiance");
    public static final SoundEvent ACTUALLY_CAST = sound("casting.cast");

    public static final SoundEvent ABACUS = sound("abacus");
    public static final SoundEvent ABACUS_SHAKE = sound("abacus.shake");

    public static final SoundEvent SPELL_CIRCLE_FIND_BLOCK = sound("spellcircle.find_block");
    public static final SoundEvent SPELL_CIRCLE_FAIL = sound("spellcircle.fail");
    public static final SoundEvent SPELL_CIRCLE_CAST = sound("spellcircle.cast");

    public static final SoundEvent SCROLL_DUST = sound("scroll.dust");
    public static final SoundEvent SCROLL_SCRIBBLE = sound("scroll.scribble");

    public static final SoundEvent IMPETUS_LOOK_TICK = sound("impetus.fletcher.tick");
    public static final SoundEvent IMPETUS_STOREDPLAYER_DING = sound("impetus.cleric.register");

    public static final SoundEvent READ_LORE_FRAGMENT = sound("lore_fragment.read");

    private static SoundEvent sound(String name) {
        var id = modLoc(name);
        var sound = new SoundEvent(id);
        var old = SOUNDS.put(id, sound);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return sound;
    }
}
