package at.petrak.hexcasting.common.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ConjureParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;
    private final boolean light;

    ConjureParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet pSprites, boolean light) {
        super(pLevel, pX, pY, pZ, (0.5D - RANDOM.nextDouble()) * .002, 0, (0.5D - RANDOM.nextDouble()) * .002);
        this.friction = 0.96F;
        this.gravity = light ? -0.01F : 0F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = pSprites;
        this.yd *= 0F;
        this.xd *= light ? 0.001f : 0.1F;
        this.zd *= light ? 0.001f : 0.1F;
        this.roll = RANDOM.nextFloat(360);
        this.oRoll = this.roll;
        this.light = light;
        this.quadSize *= light ? 0.9f : 0.75f;

        this.lifetime = (int) ((light ? 64.0D : 32.0D) / ((Math.random() + 3f) * 0.25f));
        this.hasPhysics = false;
        this.setSpriteFromAge(pSprites);
    }

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (light) this.quadSize *= 0.96f;
    }

    public void setSpriteFromAge(@NotNull SpriteSet pSprite) {
        if (!this.removed) {
            int age = this.age * 4;
            if (age > this.lifetime) age /= 4;
            this.setSprite(pSprite.get(age, this.lifetime));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class BlockProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public BlockProvider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(@NotNull SimpleParticleType pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            ConjureParticle particle = new ConjureParticle(pLevel, pX, pY, pZ, this.sprite, false);
            particle.setColor((float) pXSpeed, (float) pYSpeed, (float) pZSpeed);
            return particle;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LightProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public LightProvider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(@NotNull SimpleParticleType pType, @NotNull ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            ConjureParticle particle = new ConjureParticle(pLevel, pX, pY, pZ, this.sprite, true);
            particle.setColor((float) pXSpeed, (float) pYSpeed, (float) pZSpeed);
            return particle;
        }
    }
}
