package at.petrak.hexcasting.api.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexAdvancementTriggers {
    private static final Map<ResourceLocation, CriterionTrigger<?>> TRIGGERS = new LinkedHashMap<>();

    public static final OvercastTrigger OVERCAST_TRIGGER = register("overcast", new OvercastTrigger());
    public static final SpendMediaTrigger SPEND_MEDIA_TRIGGER = register("spend_media", new SpendMediaTrigger());
    public static final FailToCastGreatSpellTrigger FAIL_GREAT_SPELL_TRIGGER = register("fail_to_cast_great_spell", new FailToCastGreatSpellTrigger());

    public static void registerTriggers(BiConsumer<CriterionTrigger<?>, ResourceLocation> r) {
        for (var e : TRIGGERS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static <T extends CriterionTrigger<?>> T register(
            String id,
            T lift
    ) {
        var old = TRIGGERS.put(modLoc(id), lift);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return lift;
    }
}
