package com.strangesmell.valorant.leizhi.bigbomb;

import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BigBombRendererState extends EntityRenderState {
    public final SkullModelBase.State skull = new SkullModelBase.State();
    public float xRot;
    public float yRot;
}
