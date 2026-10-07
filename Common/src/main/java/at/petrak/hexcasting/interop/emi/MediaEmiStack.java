package at.petrak.hexcasting.interop.emi;

import at.petrak.hexcasting.interop.patchouli.PatchouliUtils;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.render.EmiRender;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.screen.tooltip.IngredientTooltipComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class MediaEmiStack implements EmiIngredient{
    private List<EmiStack> ingredients;
    private long mediaCost;

    public MediaEmiStack(long mediaCost){
        setAmount(mediaCost);
    }

    @Override
    public boolean equals(Object obj){
        return obj instanceof MediaEmiStack mes && this.mediaCost == mes.mediaCost;
    }

    @Override
    public int hashCode(){
        return ingredients.hashCode();
    }

    @Override
    public String toString(){
        return mediaCost + " Media";
    }

    @Override
    public List<EmiStack> getEmiStacks(){
        return ingredients;
    }

    @Override
    public EmiIngredient copy(){
        return new MediaEmiStack(mediaCost);
    }

    @Override
    public long getAmount(){
        return mediaCost;
    }

    @Override
    public EmiIngredient setAmount(long amount){
        this.mediaCost = amount;
        ingredients = PatchouliUtils.mediaItems(amount, true).stream().map(EmiStack::of).toList();
        return this;
    }

    @Override
    public float getChance(){
        return 1;
    }

    @Override
    public EmiIngredient setChance(float chance){
        return this;
    }

    // Taken from ListEmiIngredient
    @Override
    public void render(GuiGraphics draw, int x, int y, float delta, int flags){
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        EmiIngredient current = ingredients.get(item);
        if ((flags & RENDER_ICON) != 0) {
            current.render(draw, x, y, delta, ~RENDER_AMOUNT);
        }
        if ((flags & RENDER_AMOUNT) != 0) {
            current.render(draw, x, y, delta, RENDER_AMOUNT);
        }
        if ((flags & RENDER_INGREDIENT) != 0) {
            EmiRender.renderIngredientIcon(this, draw, x, y);
        }
    }

    // Taken from ListEmiIngredient
    @Override
    public List<ClientTooltipComponent> getTooltip(){
        List<ClientTooltipComponent> tooltip = Lists.newArrayList();
        tooltip.add(ClientTooltipComponent.create(EmiPort.ordered(EmiPort.translatable("tooltip.emi.accepts"))));
        tooltip.add(new IngredientTooltipComponent(ingredients));
        int item = (int) (System.currentTimeMillis() / 1000 % ingredients.size());
        tooltip.addAll(ingredients.get(item).getTooltip());
        return tooltip;
    }
}
