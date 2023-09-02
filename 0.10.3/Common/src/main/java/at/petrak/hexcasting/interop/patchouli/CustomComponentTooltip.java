package at.petrak.hexcasting.interop.patchouli;

import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IVariable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class CustomComponentTooltip implements ICustomComponent {
    int width, height;

    @SerializedName("tooltip")
    IVariable tooltipReference;

    transient IVariable tooltipVar;
    transient List<Component> tooltip;

    transient int x, y;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
        tooltip = new ArrayList<>();
        for (IVariable s : tooltipVar.asListOrSingleton()) {
            tooltip.add(s.as(Component.class));
        }
    }

    @Override
    public void render(PoseStack ms, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
        if (context.isAreaHovered(mouseX, mouseY, x, y, width, height)) {
            context.setHoverTooltipComponents(tooltip);
        }
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup) {
        tooltipVar = lookup.apply(tooltipReference);
    }
}
