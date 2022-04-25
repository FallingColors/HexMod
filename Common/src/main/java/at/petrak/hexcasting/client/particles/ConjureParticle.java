package at.petrak.hexcasting.client.particles;

import at.petrak.hexcasting.HexMod;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class ConjureParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();

    private final SpriteSet sprites;
    private final boolean light;

    ConjureParticle(ClientLevel pLevel, double x, double y, double z, double dx, double dy, double dz,
        SpriteSet pSprites, int color, boolean light) {
        super(pLevel, x, y, z, dx, dy, dz);
        this.light = light;
        this.quadSize *= light ? 0.9f : 0.75f;
        this.setParticleSpeed(dx, dy, dz);

        var lightness = light ? 0.3f : 1.0f;
        var r = FastColor.ARGB32.red(color);
        var g = FastColor.ARGB32.green(color);
        var b = FastColor.ARGB32.blue(color);
        var a = FastColor.ARGB32.alpha(color);
        this.setColor(r / 255f * lightness, g / 255f * lightness, b / 255f * lightness);
        this.setAlpha(a / 255f * lightness);

        this.friction = 0.96F;
        this.gravity = light ? -0.01F : 0F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = pSprites;

        this.roll = RANDOM.nextFloat(360);
        this.oRoll = this.roll;

        this.lifetime = (int) ((light ? 64.0D : 32.0D) / ((Math.random() + 3f) * 0.25f));
        this.hasPhysics = false;
        this.setSpriteFromAge(pSprites);
    }

    public @NotNull ParticleRenderType getRenderType() {
        return this.light ? LIGHT_RENDER_TYPE : CONJURE_RENDER_TYPE;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = 1.0f - ((float) this.age / (float) this.lifetime);
        if (light) {
            this.quadSize *= 0.96f;
        }
    }

    public void setSpriteFromAge(@NotNull SpriteSet pSprite) {
        if (!this.removed) {
            int age = this.age * 4;
            if (age > this.lifetime) {
                age /= 4;
            }
            this.setSprite(pSprite.get(age, this.lifetime));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<ConjureParticleOptions> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Nullable
        @Override
        public Particle createParticle(ConjureParticleOptions type, ClientLevel level,
            double pX, double pY, double pZ,
            double pXSpeed, double pYSpeed, double pZSpeed) {
            return new ConjureParticle(level, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, this.sprite, type.color(),
                type.isLight());
        }
    }

    // pretty sure this prevents the gross culling
    // https://github.com/VazkiiMods/Psi/blob/1.18/src/main/java/vazkii/psi/client/fx/FXWisp.java
    private record ConjureRenderType(boolean light) implements ParticleRenderType {
        @Override
        public void begin(BufferBuilder buf, TextureManager texMan) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.depthMask(false);
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            texMan.bindForSetup(TextureAtlas.LOCATION_PARTICLES);
            texMan.getTexture(TextureAtlas.LOCATION_PARTICLES).setFilter(this.light, false);
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tess) {
            tess.end();
            Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_PARTICLES)
                .restoreLastBlurMipmap();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return HexMod.MOD_ID + (light ? ":light" : ":conjure");
        }
    }

    private static final ConjureRenderType CONJURE_RENDER_TYPE = new ConjureRenderType(false);
    private static final ConjureRenderType LIGHT_RENDER_TYPE = new ConjureRenderType(true);
}
