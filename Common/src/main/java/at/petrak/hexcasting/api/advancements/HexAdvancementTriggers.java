package at.petrak.hexcasting.api.advancements;

import at.petrak.hexcasting.xplat.IXplatAbstractions;
import at.petrak.hexcasting.xplat.IXplatRegistrar;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;

public class HexAdvancementTriggers {
    private static final IXplatRegistrar<CriterionTrigger<?>> REGISTRAR = IXplatAbstractions.INSTANCE.createRegistrar(Registries.TRIGGER_TYPE);

    public static void register() {
        REGISTRAR.registerAll();
    }

    public static final Supplier<OvercastTrigger> OVERCAST_TRIGGER = REGISTRAR.register("overcast", OvercastTrigger::new);
    public static final Supplier<SpendMediaTrigger> SPEND_MEDIA_TRIGGER = REGISTRAR.register("spend_media", SpendMediaTrigger::new);
    public static final Supplier<FailToCastGreatSpellTrigger> FAIL_GREAT_SPELL_TRIGGER = REGISTRAR.register("fail_to_cast_great_spell", FailToCastGreatSpellTrigger::new);
}
