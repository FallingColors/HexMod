package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.ktxt.AccessorWrappers;

public class HexAdvancementTriggers {
    public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();
    public static final SpendManaTrigger SPEND_MANA_TRIGGER = new SpendManaTrigger();
    public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = new FailToCastGreatSpellTrigger();

    public static void registerTriggers() {
        AccessorWrappers.registerCriteriaTrigger(OVERCAST_TRIGGER);
        AccessorWrappers.registerCriteriaTrigger(SPEND_MANA_TRIGGER);
        AccessorWrappers.registerCriteriaTrigger(FAIL_GREAT_SPELL_TRIGGER);
    }
}
