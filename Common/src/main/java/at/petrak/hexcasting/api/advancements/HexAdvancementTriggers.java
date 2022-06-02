package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.mixin.accessor.CriteriaTriggersAccessor;

public class HexAdvancementTriggers {
    public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();
    public static final SpendManaTrigger SPEND_MANA_TRIGGER = new SpendManaTrigger();
    public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = new FailToCastGreatSpellTrigger();

    public static void registerTriggers() {
        CriteriaTriggersAccessor.hex$register(OVERCAST_TRIGGER);
        CriteriaTriggersAccessor.hex$register(SPEND_MANA_TRIGGER);
        CriteriaTriggersAccessor.hex$register(FAIL_GREAT_SPELL_TRIGGER);
    }
}
