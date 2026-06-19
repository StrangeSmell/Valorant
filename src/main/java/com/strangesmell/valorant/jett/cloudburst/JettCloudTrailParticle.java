package com.strangesmell.valorant.jett.cloudburst;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class JettCloudTrailParticle extends SingleQuadParticle {
    public JettCloudTrailParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.scale(2.8F + this.random.nextFloat() * 1.0F);
        this.setSize(0.35F, 0.35F);
        this.lifetime = this.random.nextInt(24) + 44;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = xSpeed * 0.04D;
        this.yd = ySpeed * 0.04D;
        this.zd = zSpeed * 0.04D;
        this.setAlpha(0.82F);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime || this.alpha <= 0.0F) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.92D;
        this.yd *= 0.92D;
        this.zd *= 0.92D;
        if (this.age >= this.lifetime - 20) {
            this.alpha = Math.max(0.0F, this.alpha - 0.04F);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new JettCloudTrailParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites.get(random));
        }
    }
}
