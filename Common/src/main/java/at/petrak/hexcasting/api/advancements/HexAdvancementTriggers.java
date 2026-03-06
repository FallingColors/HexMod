package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.mixin.accessor.CriteriaTriggersAccessor;

public class HexAdvancementTriggers {
    public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();
    public static final SpendMediaTrigger SPEND_MEDIA_TRIGGER = new SpendMediaTrigger();
    public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = new FailToCastGreatSpellTrigger();

    public static void registerTriggers() {
        CriteriaTriggersAccessor.hex$register("hexcasting:overcast", OVERCAST_TRIGGER);
        CriteriaTriggersAccessor.hex$register("hexcasting:spend_media", SPEND_MEDIA_TRIGGER);
        CriteriaTriggersAccessor.hex$register("hexcasting:fail_to_cast_great_spell", FAIL_GREAT_SPELL_TRIGGER);
    }
}
