package at.petrak.hexcasting.common.lib;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexSounds {
    private static final IXplatRegistrar<SoundEvent> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(Registries.SOUND_EVENT);

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Holder<SoundEvent> START_PATTERN = REGISTRAR.registerHolder("casting.pattern.start", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.pattern.start")));
    public static final Holder<SoundEvent> ADD_TO_PATTERN = REGISTRAR.registerHolder("casting.pattern.add_segment", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.pattern.add_segment")));

    public static final Holder<SoundEvent> CASTING_AMBIANCE = REGISTRAR.registerHolder("casting.ambiance", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.ambiance")));

    public static final Holder<SoundEvent> CAST_NORMAL = REGISTRAR.registerHolder("casting.cast.normal", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.normal")));
    public static final Holder<SoundEvent> CAST_SPELL = REGISTRAR.registerHolder("casting.cast.spell", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.spell")));
    public static final Holder<SoundEvent> CAST_HERMES = REGISTRAR.registerHolder("casting.cast.hermes", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.hermes")));
    public static final Holder<SoundEvent> CAST_THOTH = REGISTRAR.registerHolder("casting.cast.thoth", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.thoth")));
    public static final Holder<SoundEvent> CAST_FAILURE = REGISTRAR.registerHolder("casting.cast.fail", () ->
        SoundEvent.createVariableRangeEvent(modLoc("casting.cast.fail")));

    public static final Holder<SoundEvent> ABACUS = REGISTRAR.registerHolder("abacus", () ->
        SoundEvent.createVariableRangeEvent(modLoc("abacus")));
    public static final Holder<SoundEvent> ABACUS_SHAKE = REGISTRAR.registerHolder("abacus.shake", () ->
        SoundEvent.createVariableRangeEvent(modLoc("abacus.shake")));

    public static final Holder<SoundEvent> STAFF_RESET = REGISTRAR.registerHolder("staff.reset", () ->
        SoundEvent.createVariableRangeEvent(modLoc("staff.reset")));

    public static final Holder<SoundEvent> SPELL_CIRCLE_FIND_BLOCK = REGISTRAR.registerHolder("spellcircle.find_block", () ->
        SoundEvent.createVariableRangeEvent(modLoc("spellcircle.find_block")));
    public static final Holder<SoundEvent> SPELL_CIRCLE_FAIL = REGISTRAR.registerHolder("spellcircle.fail", () ->
        SoundEvent.createVariableRangeEvent(modLoc("spellcircle.fail")));

    public static final Holder<SoundEvent> SCROLL_DUST = REGISTRAR.registerHolder("scroll.dust", () ->
        SoundEvent.createVariableRangeEvent(modLoc("scroll.dust")));
    public static final Holder<SoundEvent> SCROLL_SCRIBBLE = REGISTRAR.registerHolder("scroll.scribble", () ->
        SoundEvent.createVariableRangeEvent(modLoc("scroll.scribble")));

    public static final Holder<SoundEvent> IMPETUS_LOOK_TICK = REGISTRAR.registerHolder("impetus.fletcher.tick", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.fletcher.tick")));
    public static final Holder<SoundEvent> IMPETUS_REDSTONE_DING = REGISTRAR.registerHolder("impetus.redstone.register", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.redstone.register")));
    public static final Holder<SoundEvent> IMPETUS_REDSTONE_CLEAR = REGISTRAR.registerHolder("impetus.redstone.clear", () ->
        SoundEvent.createVariableRangeEvent(modLoc("impetus.redstone.clear")));

    public static final Holder<SoundEvent> READ_LORE_FRAGMENT = REGISTRAR.registerHolder("lore_fragment.read", () ->
        SoundEvent.createVariableRangeEvent(modLoc("lore_fragment.read")));

    public static final Holder<SoundEvent> FLIGHT_AMBIENCE = REGISTRAR.registerHolder("flight.ambience", () ->
        SoundEvent.createVariableRangeEvent(modLoc("flight.ambience")));
    public static final Holder<SoundEvent> FLIGHT_FINISH = REGISTRAR.registerHolder("flight.finish", () ->
        SoundEvent.createVariableRangeEvent(modLoc("flight.finish")));
}
