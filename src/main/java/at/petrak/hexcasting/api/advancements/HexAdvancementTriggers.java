package at.petrak.hexcasting.api.advancements;

import net.minecraft.advancements.CriteriaTriggers;

public class HexAdvancementTriggers {
	public static final OvercastTrigger OVERCAST_TRIGGER = new OvercastTrigger();
	public static final SpendManaTrigger SPEND_MANA_TRIGGER = new SpendManaTrigger();
	public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = new FailToCastGreatSpellTrigger();

	public static void registerTriggers() {
		CriteriaTriggers.register(OVERCAST_TRIGGER);
		CriteriaTriggers.register(SPEND_MANA_TRIGGER);
		CriteriaTriggers.register(FAIL_GREAT_SPELL_TRIGGER);
	}
}
