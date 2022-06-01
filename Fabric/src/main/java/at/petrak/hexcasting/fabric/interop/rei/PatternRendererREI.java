package at.petrak.hexcasting.fabric.interop.rei;

import at.petrak.hexcasting.api.PatternRegistry;
import at.petrak.hexcasting.api.spell.math.HexCoord;
import at.petrak.hexcasting.interop.utils.PatternDrawingUtil;
import at.petrak.hexcasting.interop.utils.PatternEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class PatternRendererREI implements Renderer {

    private final long startTime = System.currentTimeMillis();

    private final int width;
    private final int height;

    private final boolean strokeOrder;

    private final List<PatternEntry> patterns;
    private final List<Vec2> pathfinderDots;

    public PatternRendererREI(ResourceLocation pattern, int w, int h) {
        var entry = PatternRegistry.lookupPattern(pattern);
        this.strokeOrder = !entry.isPerWorld();
        var data = PatternDrawingUtil.loadPatterns(List.of(new Pair<>(entry.getPrototype(), HexCoord.getOrigin())));
        this.patterns = data.patterns();
        this.pathfinderDots = data.pathfinderDots();
        this.width = w;
        this.height = h;
    }

    @Environment(EnvType.CLIENT)
    private int blitOffset;

    @Override
    @Environment(EnvType.CLIENT)
    public int getZ() {
        return blitOffset;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void setZ(int z) {
        this.blitOffset = z;
    }

    @Override
    public void render(PoseStack matrices, Rectangle bounds, int mouseX, int mouseY, float delta) {
        long time = (System.currentTimeMillis() - startTime) / 50;
        matrices.pushPose();
        matrices.translate(bounds.getMinX() - 0.5f + width / 2f, bounds.getMinY() + height / 2f, blitOffset);
        matrices.scale(width / 64f, height / 64f, 1f);
        PatternDrawingUtil.drawPattern(matrices, 0, 0, this.patterns, this.pathfinderDots, this.strokeOrder, time,
            0xff_333030, 0xff_191818, 0xc8_0c0a0c, 0x80_666363);
        matrices.popPose();
    }
}
