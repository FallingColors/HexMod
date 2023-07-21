package at.petrak.hexcasting.client.render.overlays;

import at.petrak.hexcasting.client.render.shader.HexShaders;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import org.joml.Matrix4f;

/**
 * How eigengrau works
 * - EIGENGRAU_BZ_SIMULATION stores the state of the BZ cellular automata (thanks Acerola). It's updated on the GPU
 * with the eigengrau_bz shader.
 * - EIGENGRAU_VEIL uses the red channel to control the minimum brightness required to show eigengrau, and the green
 * channel to control how much eigengrau is shown on top of that.
 */
public class EigengrauOverlay {
    public static TextureTarget EIGENGRAU_BZ_SIMULATION;
    public static TextureTarget EIGENGRAU_VEIL;
    /**
     * "Radius" of a hex cell in pixels
     */
    private static final int RESOLUTION = 5;

    public static void renderEigengrau() {
        var window = Minecraft.getInstance().getWindow();
        int w = window.getWidth();
        int h = window.getHeight();

        // Tick BZ simulation
        var prevShader = RenderSystem.getShader();
        RenderSystem.setShaderTexture(0, EIGENGRAU_BZ_SIMULATION.getColorTextureId());
        HexShaders.EIGENGRAU_BZ.getUniform("ScreenSize").set((float) w, (float) h);
        RenderSystem.setShader(() -> HexShaders.EIGENGRAU_BZ);

        // default ctor gives identity mat
        var mat = new Matrix4f();

        var tess = Tesselator.getInstance();
        var buf = tess.getBuilder();

        renderOverAll(w, h, mat, tess, buf);

        RenderSystem.setShader(() -> HexShaders.EIGENGRAU_PRESENTER);
        RenderSystem.setShaderTexture(0, Minecraft.getInstance().getMainRenderTarget().getColorTextureId());
        RenderSystem.setShaderTexture(1, EIGENGRAU_VEIL.getColorTextureId());
        RenderSystem.setShaderTexture(2, EIGENGRAU_BZ_SIMULATION.getColorTextureId());
        HexShaders.EIGENGRAU_PRESENTER.getUniform("ScreenSize").set((float) w, (float) h);
        HexShaders.EIGENGRAU_PRESENTER.getUniform("Resolution").set((float) RESOLUTION);
        renderOverAll(w, h, mat, tess, buf);

        RenderSystem.setShader(() -> prevShader);
    }

    private static void renderOverAll(int w, int h, Matrix4f mat, Tesselator tess, BufferBuilder buf) {
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(mat, 0, 0, 0)
            .uv(0, 0)
            .endVertex();
        buf.vertex(mat, 0, h, 0)
            .uv(0, 1)
            .endVertex();
        buf.vertex(mat, w, h, 0)
            .uv(1, 1)
            .endVertex();
        buf.vertex(mat, w, 0, 0)
            .uv(1, 0)
            .endVertex();
        tess.end();
    }

    public static void initTextures() {
        EIGENGRAU_BZ_SIMULATION = new TextureTarget(1, 1, true, Minecraft.ON_OSX);
        EIGENGRAU_VEIL = new TextureTarget(1, 1, true, Minecraft.ON_OSX);
        resizeTextures();
    }

    public static void resizeTextures() {
        var mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        int bzWidth = width / RESOLUTION;
        int bzHeight = height / RESOLUTION;

        EIGENGRAU_VEIL.resize(width, height, Minecraft.ON_OSX);
        EIGENGRAU_BZ_SIMULATION.resize(bzWidth, bzHeight, Minecraft.ON_OSX);
    }
}
