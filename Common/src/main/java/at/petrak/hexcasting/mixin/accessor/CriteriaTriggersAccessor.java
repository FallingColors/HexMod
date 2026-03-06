package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = CriteriaTriggers.class, remap = false)
public interface CriteriaTriggersAccessor {
    @Invoker("register")
    static <T extends CriterionTrigger<?>> T hex$register(String id, T trigger) {
        throw new UnsupportedOperationException();
    }
}
