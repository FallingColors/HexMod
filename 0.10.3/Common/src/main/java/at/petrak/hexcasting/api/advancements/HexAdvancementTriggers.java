package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.mixin.accessor.CriteriaTriggersAccessor;

public class HexAdvancementTriggers {
    public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();
    public static final SpendMediaTrigger SPEND_MEDIA_TRIGGER = new SpendMediaTrigger();
    public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = new FailToCastGreatSpellTrigger();

    public static void registerTriggers() {
        CriteriaTriggersAccessor.hex$register(OVERCAST_TRIGGER);
        CriteriaTriggersAccessor.hex$register(SPEND_MEDIA_TRIGGER);
        CriteriaTriggersAccessor.hex$register(FAIL_GREAT_SPELL_TRIGGER);
    }
}
