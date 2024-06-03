/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/client/java/aztech/modern_industrialization/util/RenderHelper.java
 * Thanks!
 */

package net.natte.tankstorage.rendering;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

public class FluidRenderer {

    public static void drawFluidInGui(DrawContext context, FluidVariant fluidVariant, int x, int y) {
        float i = x;
        float j = y;
        int scale = 16;
        int fractionUp = 1;
        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

        Sprite sprite = FluidVariantRendering.getSprite(fluidVariant);
        int color = FluidVariantRendering.getColor(fluidVariant);

        if (sprite == null)
            return;

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        float x0 = i;
        float y0 = j;
        float x1 = x0 + scale;
        float y1 = y0 + scale * fractionUp;
        float z = 0.5f;
        float u0 = sprite.getMinU();
        float v1 = sprite.getMaxV();
        float v0 = v1 + (sprite.getMinV() - v1) * fractionUp;
        float u1 = sprite.getMaxU();

        Matrix4f model = context.getMatrices().peek().getPositionMatrix();
        bufferBuilder.vertex(model, x0, y1, z).color(r, g, b, 1).texture(u0, v1).next();
        bufferBuilder.vertex(model, x1, y1, z).color(r, g, b, 1).texture(u1, v1).next();
        bufferBuilder.vertex(model, x1, y0, z).color(r, g, b, 1).texture(u1, v0).next();
        bufferBuilder.vertex(model, x0, y0, z).color(r, g, b, 1).texture(u0, v0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        RenderSystem.enableDepthTest();

        // SodiumCompat.markSpriteActive(sprite);
    }

    public static void drawFluidCount(TextRenderer textRenderer, DrawContext context, long amount, int x, int y) {
        // TODO
        // count format (1M, 1.1M 1000B) or whatever format I decide
        // scale
        
        Text countText = getFormattedFluidCount(amount);
        int textWidth = textRenderer.getWidth(countText);
        int xOffset = x + 18 - 2;
        int yOffset = y + 18 - 2;
        MinecraftClient client = MinecraftClient.getInstance();
        int guiScale = (int) client.getWindow().getScaleFactor();
        // float scale = guiScale == 1 ? 0.5f : (guiScale - 1) / (float) guiScale;
        float scale = guiScale == 1 ? 0.5f : (int) (guiScale * 0.7f) / (float) guiScale;
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(xOffset, yOffset, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-xOffset, -yOffset, 0);
        context.drawText(textRenderer, countText, x + 18 - 1 - textWidth - 1, y + 9 - 1, Colors.WHITE, true);
        matrices.pop();
    }

    private static Text getFormattedFluidCount(long amount){
        return Text.of("" + (double)amount / FluidConstants.BUCKET + "B");
    }

}