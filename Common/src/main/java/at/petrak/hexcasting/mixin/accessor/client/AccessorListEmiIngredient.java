package at.petrak.hexcasting.mixin.accessor.client;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.ListEmiIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ListEmiIngredient.class)
public interface AccessorListEmiIngredient{
    @Accessor("ingredients")
    List<EmiIngredient> hex$ingredients();
}
