package com.strangesmell.valorant;

import net.minecraft.core.particles.DustParticleOptions;


public final class ValorantParticles {
    public static final DustParticleOptions LEIZHI_YELLOW = dust(0xFFFFD21F, 1.0F);
    public static final DustParticleOptions LEIZHI_ORANGE = dust(0xFFFF6A00, 1.25F);
    public static final DustParticleOptions CLOVE_PURPLE = dust(0xFF7C4DFF, 1.0F);
    public static final DustParticleOptions CLOVE_LILAC = dust(0xFFB477FF, 1.1F);
    public static final DustParticleOptions CLOVE_PINK = dust(0xFFFF66FF, 1.15F);
    public static final DustParticleOptions CLOVE_MINT = dust(0xFF80FFCC, 1.1F);
    public static final DustParticleOptions SAGE_TEAL = dust(0xFF66FFD9, 1.1F);

    private ValorantParticles() {
    }

    private static DustParticleOptions dust(int argb, float scale) {
        return new DustParticleOptions(argb, scale);
    }
}
