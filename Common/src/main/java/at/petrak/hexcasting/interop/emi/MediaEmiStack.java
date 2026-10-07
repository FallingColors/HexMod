package at.petrak.hexcasting.interop.emi;

import at.petrak.hexcasting.mixin.accessor.client.AccessorListEmiIngredient;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.ListEmiIngredient;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class MediaEmiStack extends ListEmiIngredient{
    public MediaEmiStack(List<? extends EmiIngredient> ingredients){
        super(ingredients, 0);
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags){
        List<EmiIngredient> ingredients = ((AccessorListEmiIngredient)this).hex$ingredients();
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        EmiIngredient current = ingredients.get(item);
        if ((flags & RENDER_ICON) != 0) {
            current.render(draw, x, y, delta, -1 ^ RENDER_AMOUNT);
        }
        if ((flags & RENDER_AMOUNT) != 0) {
            current.render(draw, x, y, delta, RENDER_AMOUNT);
        }
        if ((flags & RENDER_INGREDIENT) != 0) {
            EmiRender.renderIngredientIcon(this, draw, x, y);
        }
    }
}
