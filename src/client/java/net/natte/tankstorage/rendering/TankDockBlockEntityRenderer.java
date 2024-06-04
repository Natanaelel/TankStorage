package net.natte.tankstorage.rendering;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.natte.tankstorage.block.TankDockBlockEntity;

public class TankDockBlockEntityRenderer implements BlockEntityRenderer<TankDockBlockEntity> {

    public TankDockBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(TankDockBlockEntity tankDock, float timeDelta, MatrixStack matrixStack,
            VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        matrixStack.push();
        matrixStack.translate(0.5f, 0.5f, 0.5f);
        // prevent z-fighting
        float scale = 1f - 0.0001f;
        matrixStack.scale(scale, scale, scale);

        itemRenderer.renderItem(tankDock.getTank(), ModelTransformationMode.FIXED,
                light, overlay, matrixStack, vertexConsumers, null, 0);

        matrixStack.pop();
    }

}
