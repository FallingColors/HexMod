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

    public static final SoundEvent START_PATTERN = sound("casting.pattern.start");
    public static final SoundEvent ADD_TO_PATTERN = sound("casting.pattern.add_segment");

    public static final SoundEvent CASTING_AMBIANCE = sound("casting.ambiance");

    public static final SoundEvent CAST_NORMAL = sound("casting.cast.normal");
    public static final SoundEvent CAST_SPELL = sound("casting.cast.spell");
    public static final SoundEvent CAST_HERMES = sound("casting.cast.hermes");
    public static final SoundEvent CAST_THOTH = sound("casting.cast.thoth");
    public static final SoundEvent CAST_FAILURE = sound("casting.cast.fail");

    public static final SoundEvent ABACUS = sound("abacus");
    public static final SoundEvent ABACUS_SHAKE = sound("abacus.shake");

    public static final SoundEvent STAFF_RESET = sound("staff.reset");

    public static final SoundEvent SPELL_CIRCLE_FIND_BLOCK = sound("spellcircle.find_block");
    public static final SoundEvent SPELL_CIRCLE_FAIL = sound("spellcircle.fail");

    public static final SoundEvent SCROLL_DUST = sound("scroll.dust");
    public static final SoundEvent SCROLL_SCRIBBLE = sound("scroll.scribble");

    public static final SoundEvent IMPETUS_LOOK_TICK = sound("impetus.fletcher.tick");
    public static final SoundEvent IMPETUS_REDSTONE_DING = sound("impetus.redstone.register");

    public static final SoundEvent READ_LORE_FRAGMENT = sound("lore_fragment.read");

    public static final SoundEvent FLIGHT_AMBIENCE = sound("flight.ambience");
    public static final SoundEvent FLIGHT_FINISH = sound("flight.finish");

    private static SoundEvent sound(String name) {
        var id = modLoc(name);
        var sound = SoundEvent.createVariableRangeEvent(id);
        var old = SOUNDS.put(id, sound);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return sound;
    }
}
