package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegister;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexSounds {
    private static final IXplatRegister<SoundEvent> REGISTER = IXplatAbstractions.INSTANCE.createRegistar(Registries.SOUND_EVENT);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Holder<SoundEvent> START_PATTERN = REGISTER.registerHolder("casting.pattern.start", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.pattern.start")));
    public static final Holder<SoundEvent> ADD_TO_PATTERN = REGISTER.registerHolder("casting.pattern.add_segment", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.pattern.add_segment")));

    public static final Holder<SoundEvent> CASTING_AMBIANCE = REGISTER.registerHolder("casting.ambiance", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.ambiance")));

    public static final Holder<SoundEvent> CAST_NORMAL = REGISTER.registerHolder("casting.cast.normal", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.normal")));
    public static final Holder<SoundEvent> CAST_SPELL = REGISTER.registerHolder("casting.cast.spell", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.spell")));
    public static final Holder<SoundEvent> CAST_HERMES = REGISTER.registerHolder("casting.cast.hermes", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.hermes")));
    public static final Holder<SoundEvent> CAST_THOTH = REGISTER.registerHolder("casting.cast.thoth", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.thoth")));
    public static final Holder<SoundEvent> CAST_FAILURE = REGISTER.registerHolder("casting.cast.fail", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.fail")));

    public static final Holder<SoundEvent> ABACUS = REGISTER.registerHolder("abacus", () ->
        SoundEvent.createVariableRangeEvent(modLoc("abacus")));
    public static final Holder<SoundEvent> ABACUS_SHAKE = REGISTER.registerHolder("abacus.shake", () ->
        SoundEvent.createVariableRangeEvent(modLoc("abacus.shake")));

    public static final Holder<SoundEvent> STAFF_RESET = REGISTER.registerHolder("staff.reset", () ->
        SoundEvent.createVariableRangeEvent(modLoc("staff.reset")));

    public static final Holder<SoundEvent> SPELL_CIRCLE_FIND_BLOCK = REGISTER.registerHolder("spellcircle.find_block", () ->
        SoundEvent.createVariableRangeEvent(modLoc("spellcircle.find_block")));
    public static final Holder<SoundEvent> SPELL_CIRCLE_FAIL = REGISTER.registerHolder("spellcircle.fail", () ->
        SoundEvent.createVariableRangeEvent(modLoc("spellcircle.fail")));

    public static final Holder<SoundEvent> SCROLL_DUST = REGISTER.registerHolder("scroll.dust", () ->
        SoundEvent.createVariableRangeEvent(modLoc("scroll.dust")));
    public static final Holder<SoundEvent> SCROLL_SCRIBBLE = REGISTER.registerHolder("scroll.scribble", () ->
        SoundEvent.createVariableRangeEvent(modLoc("scroll.scribble")));

    public static final Holder<SoundEvent> IMPETUS_LOOK_TICK = REGISTER.registerHolder("impetus.fletcher.tick", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.fletcher.tick")));
    public static final Holder<SoundEvent> IMPETUS_REDSTONE_DING = REGISTER.registerHolder("impetus.redstone.register", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.redstone.register")));
    public static final Holder<SoundEvent> IMPETUS_REDSTONE_CLEAR = REGISTER.registerHolder("impetus.redstone.clear", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.redstone.clear")));

    public static final Holder<SoundEvent> READ_LORE_FRAGMENT = REGISTER.registerHolder("lore_fragment.read", () ->
        SoundEvent.createVariableRangeEvent(modLoc("lore_fragment.read")));

    public static final Holder<SoundEvent> FLIGHT_AMBIENCE = REGISTER.registerHolder("flight.ambience", () ->
        SoundEvent.createVariableRangeEvent(modLoc("flight.ambience")));
    public static final Holder<SoundEvent> FLIGHT_FINISH = REGISTER.registerHolder("flight.finish", () ->
        SoundEvent.createVariableRangeEvent(modLoc("flight.finish")));
}
