package net.natte.tankstorage.rendering;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;

public class HudRenderer {

    private static final Identifier WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    private MinecraftClient client;

    @SuppressWarnings("unused")
    private UUID uuid;
    private CachedFluidStorageState tank;
    private TankOptions options;

    private Arm arm;

    public void reset() {
        this.client = null;
    }

    public void tick(MinecraftClient minecraftClient) {
        if (client == null)
            client = minecraftClient;

        if (client.player == null)
            return;

        tank = null;
        options = null;

        ItemStack stack;

        if (Util.isTankLike(stack = client.player.getMainHandStack()) && Util.hasUUID(stack)) {
            arm = client.player.getMainArm();
            uuid = Util.getUUID(stack);
            options = Util.getOptionsOrDefault(stack);
        } else if (Util.isTankLike(stack = client.player.getOffHandStack()) && Util.hasUUID(stack)) {
            arm = client.player.getMainArm().getOpposite();
            uuid = Util.getUUID(stack);
            options = Util.getOptionsOrDefault(stack);
        }

        if (options != null && options.interactionMode == TankInteractionMode.BUCKET)
            tank = ClientTankCache.getAndQueueThrottledUpdate(Util.getUUID(stack), 2 * 20);

    }

    private boolean shouldRender() {
        if (tank == null)
            return false;
        if (options == null)
            return false;
        if (options.interactionMode != TankInteractionMode.BUCKET)
            return false;
        return true;
    }

    public void render(DrawContext context, float tickDelta) {
        if (!shouldRender())
            return;

        List<FluidSlotData> fluids = tank.getNonEmptyFluids();

        int scaledHeight = context.getScaledWindowHeight();
        int scaledWidth = context.getScaledWindowWidth();

        RenderSystem.enableBlend();

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();

        int selectedSlot = options.selectedSlot;

        int handXOffset = this.arm == Arm.LEFT ? -169 : 118;
        if (client.player.getMainArm() == Arm.LEFT)
            handXOffset += 29;

        if (fluids.size() == 0) {
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 + handXOffset, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == -1 || selectedSlot == fluids.size() - 1) {
            boolean isLeft = selectedSlot >= 0;
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 - (isLeft ? 20 : 0) + handXOffset, scaledHeight - 22, 22, 0, 42, 22);
        } else {
            context.drawTexture(WIDGET_TEXTURE,
                    scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64, 0, 62, 22);
        }
        try {
            for (int i = -1; i <= 1; ++i) {
                int index = selectedSlot - i;
                if (index < -1 || index >= fluids.size())
                    continue;
                int y = scaledHeight - 19;
                int x = scaledWidth / 2 - i * 20 + 3 + handXOffset;

                if (index == -1)
                    renderHotbarItem(context, x, y, tickDelta, this.client.player, Items.BUCKET.getDefaultStack(), 0);
                else
                    renderHotbarFluid(context, x, y, tickDelta, this.client.player, fluids.get(index), 0);

            }
        } catch (Exception e) {
        }

        context.drawTexture(WIDGET_TEXTURE,
                scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.pop();

        RenderSystem.disableBlend();

    }

    private void renderHotbarItem(DrawContext context, int x, int y, float tickDelta, ClientPlayerEntity player,
            ItemStack stack, int seed) {
        if (stack.isEmpty()) {
            return;
        }

        float g = (float) stack.getBobbingAnimationTime() - tickDelta;
        if (g > 0.0f) {
            float h = 1.0f + g / 5.0f;
            context.getMatrices().push();
            context.getMatrices().translate(x + 8, y + 12, 0.0f);
            context.getMatrices().scale(1.0f / h, (h + 1.0f) / 2.0f, 1.0f);
            context.getMatrices().translate(-(x + 8), -(y + 12), 0.0f);
        }
        context.drawItem((LivingEntity) player, stack, x, y, seed);
        if (g > 0.0f) {
            context.getMatrices().pop();
        }
    }

    private void renderHotbarFluid(DrawContext context, int x, int y, float tickDelta, ClientPlayerEntity player,
            FluidSlotData fluidSlotData, int i) {
        FluidRenderer.drawFluidInGui(context, fluidSlotData.fluidVariant(), x, y);
        FluidRenderer.drawFluidCount(client.textRenderer, context, fluidSlotData.amount(), x, y);
    }

}
