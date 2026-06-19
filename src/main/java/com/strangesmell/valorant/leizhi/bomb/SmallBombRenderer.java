package com.strangesmell.valorant.leizhi.bomb;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
public class SmallBombRenderer<T extends Entity & ItemSupplier> extends BombRenderer<T> {
    public SmallBombRenderer(EntityRendererProvider.Context context, float scale, boolean fullBright) {
        super(context, scale, fullBright);
    }

    public SmallBombRenderer(EntityRendererProvider.Context context) {
        this(context, 0.55F, false);
    }
}
