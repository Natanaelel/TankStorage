/*
 * Code taken from Modern Industrialization
 * https://github.com/AztechMC/Modern-Industrialization/blob/1.20.1/src/client/java/aztech/modern_industrialization/util/RenderHelper.java
 * Thanks!
 */

package net.natte.tankstorage.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.math.BigDecimal;
import java.math.MathContext;

public class FluidRenderer {

    public static void drawFluidInGui(GuiGraphics context, FluidStack fluidVariant, int x, int y) {

        float i = x;
        float j = y;
        int scale = 16;
        int fractionUp = 1;
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        TextureAtlasSprite sprite = getSprite(fluidVariant);
        int color = getColor(fluidVariant);

        if (sprite == null)
            return;

        float r = ((color >> 16) & 255) / 256f;
        float g = ((color >> 8) & 255) / 256f;
        float b = (color & 255) / 256f;
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float x0 = i;
        float y0 = j;
        float x1 = x0 + scale;
        float y1 = y0 + scale * fractionUp;
        float z = 0.5f;
        float u0 = sprite.getU0();
        float v1 = sprite.getV1();
        float v0 = v1 + (sprite.getV0() - v1) * fractionUp;
        float u1 = sprite.getU1();

        Matrix4f model = context.pose().last().pose();
        bufferBuilder.addVertex(model, x0, y1, z).setColor(r, g, b, 1).setUv(u0, v1);
        bufferBuilder.addVertex(model, x1, y1, z).setColor(r, g, b, 1).setUv(u1, v1);
        bufferBuilder.addVertex(model, x1, y0, z).setColor(r, g, b, 1).setUv(u1, v0);
        bufferBuilder.addVertex(model, x0, y0, z).setColor(r, g, b, 1).setUv(u0, v0);
        BufferUploader.draw(bufferBuilder.buildOrThrow());

        RenderSystem.enableDepthTest();

        // SodiumCompat.markSpriteActive(sprite);
    }


    public static void drawFluidCount(Font textRenderer, GuiGraphics context, long amount, int x, int y) {

        Component countText = getFormattedFluidCount(amount);
        int textWidth = textRenderer.width(countText);
        int xOffset = x + 18 - 2;
        int yOffset = y + 18 - 2;
        Minecraft client = Minecraft.getInstance();
        int guiScale = (int) client.getWindow().getGuiScale();

        float scale = guiScale == 1 ? 1f : (int) (guiScale * 0.7f) / (float) guiScale;
        PoseStack matrices = context.pose();
        matrices.pushPose();
        matrices.translate(xOffset, yOffset, 0);
        matrices.scale(scale, scale, 1);
        matrices.translate(-xOffset, -yOffset, 0);
        context.drawString(textRenderer, countText, x + 18 - 1 - textWidth - 1, y + 9 - 1, CommonColors.WHITE, true);
        matrices.popPose();
    }

    private static Component getFormattedFluidCount(long amount) {

        final int BUCKET = 1000;
        // TOD0: clean
        // not today! hah!
        var significantDigits = new MathContext(3);
        var roundedAmout = new BigDecimal(amount * 1d / BUCKET).round(significantDigits)
                .multiply(new BigDecimal(BUCKET)).longValue();
        amount = roundedAmout;
        if (amount < BUCKET) {
            double num = (long) (amount * 1000L * 1000d / BUCKET / 1000d) / 1000d;// mB
            return Component.nullToEmpty(num > 1 ? num + "" : (num + "").substring(1));
        }
        if (amount < BUCKET * 1000L) {
            var num = new BigDecimal(amount * 1d / BUCKET).round(significantDigits);
            return Component.nullToEmpty(num.toString());
        }
        if (amount < BUCKET * 1000L * 1000L) {
            var num = new BigDecimal(amount / 1000d / BUCKET).round(significantDigits);
            return Component.nullToEmpty(num.longValue() + "k");
        }
        var num = new BigDecimal(amount / 1000d / 1000d / BUCKET).round(significantDigits);
        return Component.nullToEmpty(num + "M");
    }

    @Nullable
    public static TextureAtlasSprite getSprite(FluidStack fluidVariant) {
        if (fluidVariant.isEmpty()) {
            return null;
        }
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(getExtensions(fluidVariant).getStillTexture(fluidVariant));
    }

    public static IClientFluidTypeExtensions getExtensions(FluidStack variant) {
        return IClientFluidTypeExtensions.of(variant.getFluid().getFluidType());
    }

    public static int getColor(FluidStack fluidVariant) {
        return getExtensions(fluidVariant).getTintColor(fluidVariant);
    }

}
