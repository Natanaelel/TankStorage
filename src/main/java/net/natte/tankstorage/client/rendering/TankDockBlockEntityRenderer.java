package net.natte.tankstorage.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.natte.tankstorage.block.TankDockBlockEntity;

public class TankDockBlockEntityRenderer implements BlockEntityRenderer<TankDockBlockEntity> {

    private final ItemRenderer itemRenderer;

    public TankDockBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(TankDockBlockEntity tankDock, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {

        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        // prevent z-fighting
        float scale = 0.9999f;
        poseStack.scale(scale, scale, scale);

        itemRenderer.renderStatic(tankDock.getTank(), ItemDisplayContext.FIXED, packedLight, packedOverlay, poseStack,
                buffer, null, 0);

        poseStack.popPose();
    }
}
