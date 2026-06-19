package com.strangesmell.valorant.clove.ruse;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class CloveRuseSmokeParticle extends SingleQuadParticle {
    public CloveRuseSmokeParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.scale(7.5F + this.random.nextFloat() * 2.5F);
        this.setSize(0.7F, 0.7F);
        this.lifetime = this.random.nextInt(50) + 180;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = xSpeed * 0.08D;
        this.yd = ySpeed * 0.04D;
        this.zd = zSpeed * 0.08D;
        this.setAlpha(1.0F);
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
        this.xd *= 0.94D;
        this.yd *= 0.94D;
        this.zd *= 0.94D;
        this.alpha = 1.0F;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new CloveRuseSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites.get(random));
        }
    }
}
