package at.petrak.hexcasting.mixin.accessor;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CriteriaTriggers.class)
public interface CriteriaTriggersAccessor {

    @Invoker(value = "register", remap = false)
    static <T extends CriterionTrigger<?>> T hex$register(T trigger) {
        throw new UnsupportedOperationException();
    }
}
