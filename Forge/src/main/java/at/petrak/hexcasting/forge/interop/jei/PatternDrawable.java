package at.petrak.hexcasting.forge.interop.jei;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class PatternDrawable implements IDrawable {

    private final long startTime = System.currentTimeMillis();

    private final int width;
    private final int height;

    private boolean strokeOrder;

    private final List<PatternEntry> patterns;
    private final List<Vec2> pathfinderDots;

    public PatternDrawable(ResourceLocation pattern, int w, int h) {
        var entry = PatternRegistry.lookupPattern(pattern);
        this.strokeOrder = !entry.isPerWorld();
        var data = PatternDrawingUtil.loadPatterns(List.of(new Pair<>(entry.getPrototype(), HexCoord.getOrigin())));
        this.patterns = data.patterns();
        this.pathfinderDots = data.pathfinderDots();
        this.width = w;
        this.height = h;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public PatternDrawable strokeOrder(boolean order) {
        strokeOrder = order;
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, int xOffset, int yOffset) {
        long time = (System.currentTimeMillis() - startTime) / 50;
        poseStack.pushPose();
        poseStack.translate(xOffset - 0.5f + width / 2f, yOffset + height / 2f, 0);
        poseStack.scale(width / 64f, height / 64f, 1f);
        PatternDrawingUtil.drawPattern(poseStack, 0, 0, this.patterns, this.pathfinderDots, this.strokeOrder, time,
            0xff_333030, 0xff_191818, 0xc8_0c0a0c, 0x80_666363);
        poseStack.popPose();
    }
}
