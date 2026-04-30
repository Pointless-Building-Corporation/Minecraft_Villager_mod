package com.deepan.bettervillagers.client.renderer;

import com.deepan.bettervillagers.entity.SeatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class SeatRenderer extends EntityRenderer<SeatEntity> {
    public SeatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
        this.shadowStrength = 0.0F;
    }

    @Override
    public boolean shouldRender(SeatEntity entity, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public void render(SeatEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(SeatEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
