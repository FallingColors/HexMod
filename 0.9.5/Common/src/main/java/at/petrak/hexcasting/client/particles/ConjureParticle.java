package at.petrak.hexcasting.client.particles;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.common.particles.ConjureParticleOptions;
import at.petrak.hexcasting.xplat.IClientXplatAbstractions;
import com.mojang.blaze3d.platform.GlStateManager;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        var r = FastColor.ARGB32.red(color);
        var g = FastColor.ARGB32.green(color);
        var b = FastColor.ARGB32.blue(color);
        this.setColor(r / 255f, g / 255f, b / 255f);
        this.setAlpha(light ? 0.3f : 1.0f);

        this.friction = 0.96F;
        this.gravity = light && dy != 0 && dx != 0 && dz != 0 ? -0.01F : 0F;
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
            this.alpha *= 0.3f;
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

    // https://github.com/VazkiiMods/Botania/blob/db85d778ab23f44c11181209319066d1f04a9e3d/Xplat/src/main/java/vazkii/botania/client/fx/FXWisp.java
    private record ConjureRenderType(boolean light) implements ParticleRenderType {
        @Override
        public void begin(BufferBuilder buf, TextureManager texMan) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            var tex = texMan.getTexture(TextureAtlas.LOCATION_PARTICLES);
            IClientXplatAbstractions.INSTANCE.setFilterSave(tex, this.light, false);
            buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            RenderSystem.enableDepthTest();
        }

        @Override
        public void end(Tesselator tess) {
            tess.end();
            IClientXplatAbstractions.INSTANCE.restoreLastFilter(
                Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES)
            );
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }

        @Override
        public String toString() {
            return HexAPI.MOD_ID + (light ? ":light" : ":conjure");
        }
    }

    public static final ConjureRenderType CONJURE_RENDER_TYPE = new ConjureRenderType(false);
    public static final ConjureRenderType LIGHT_RENDER_TYPE = new ConjureRenderType(true);
}
