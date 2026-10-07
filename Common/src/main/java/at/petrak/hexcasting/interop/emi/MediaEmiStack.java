package at.petrak.hexcasting.interop.emi;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

// Almost identical copy of ListEmiIngredient because the fields are private so I can't just extend ListEmiIngredient
// and the one line I need to change requires access to one of these.
public class MediaEmiStack implements EmiIngredient {
    private final List<? extends EmiIngredient> ingredients;
    private final List<EmiStack> fullList;
    private float chance = 1;

    public MediaEmiStack(List<? extends EmiIngredient> ingredients) {
        this.ingredients = ingredients;
        this.fullList = ingredients.stream().flatMap(i -> i.getEmiStacks().stream()).toList();
        if (fullList.isEmpty()) {
            throw new IllegalArgumentException("ListEmiIngredient cannot be empty");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaEmiStack other) {
            return other.getEmiStacks().equals(this.getEmiStacks());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return fullList.hashCode();
    }

    @Override
    public EmiIngredient copy() {
        EmiIngredient stack = new MediaEmiStack(ingredients);
        stack.setChance(chance);
        return stack;
    }

    @Override
    public String toString() {
        return "Ingredient" + getEmiStacks();
    }

    @Override
    public List<EmiStack> getEmiStacks() {
        return fullList;
    }

    @Override
    public long getAmount() {
        return 0;
    }

    @Override
    public EmiIngredient setAmount(long amount){
        return this;
    }

    @Override
    public float getChance(){
        return chance;
    }

    @Override
    public EmiIngredient setChance(float chance){
        this.chance = chance;
        return this;
    }

    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags) {
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

    @Override
    public List<ClientTooltipComponent> getTooltip() {
        List<ClientTooltipComponent> tooltip = Lists.newArrayList();
        tooltip.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.translatable("tooltip.emi.accepts"))));
        tooltip.add(new IngredientTooltipComponent(ingredients));
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        tooltip.addAll(ingredients.get(item).getTooltip());
        return tooltip;
    }
}
