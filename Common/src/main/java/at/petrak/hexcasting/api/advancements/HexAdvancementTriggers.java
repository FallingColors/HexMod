package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class HexAdvancementTriggers {
    private static final IXplatRegistrar<CriterionTrigger<?>> REGISTER = IXplatAbstractions.INSTANCE.createRegistrar(Registries.TRIGGER_TYPE);

    public static void register() {
        REGISTER.registerAll();
    }

    public static final Supplier<OvercastTrigger> OVERCAST_TRIGGER = REGISTER.register("overcast", OvercastTrigger::new);
    public static final Supplier<SpendMediaTrigger> SPEND_MEDIA_TRIGGER = REGISTER.register("spend_media", SpendMediaTrigger::new);
    public static final Supplier<FailToCastGreatSpellTrigger> FAIL_GREAT_SPELL_TRIGGER = REGISTER.register("fail_to_cast_great_spell", FailToCastGreatSpellTrigger::new);
}
