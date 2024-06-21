package net.natte.tankstorage.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.FluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import java.util.List;
import java.util.UUID;

public class HudRenderer {

    private static final ResourceLocation WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    private Minecraft client;

    private UUID uuid;
    private CachedFluidStorageState tank;
    private TankOptions options;
    private int selectedSlot = -1;

    private HumanoidArm arm;

    public void reset() {
        this.client = null;
    }

    public void tick() {
        if (client == null)
            client = Minecraft.getInstance();

        if (client.player == null)
            return;

        uuid = null;
        tank = null;
        options = null;

        ItemStack stack;

        if (Util.isTankLike(stack = client.player.getMainHandItem()) && Util.hasUUID(stack)) {
            arm = client.player.getMainArm();
            uuid = Util.getUUID(stack);
        } else if (Util.isTankLike(stack = client.player.getOffhandItem()) && Util.hasUUID(stack)) {
            arm = client.player.getMainArm().getOpposite();
            uuid = Util.getUUID(stack);
        }

        if (uuid != null && Util.getInteractionMode(stack) == TankInteractionMode.BUCKET) {
            tank = ClientTankCache.getAndQueueThrottledUpdate(Util.getUUID(stack), 2 * 20);
            if (tank != null)
                Util.clampSelectedSlot(stack, tank.getUniqueFluids().size() - 1);
            options = Util.getOptionsOrDefault(stack);
            selectedSlot = Util.getSelectedSlot(stack);
        }
    }

    private boolean shouldRender() {
        if (tank == null)
            return false;
        if (options == null)
            return false;
        if (options.interactionMode() != TankInteractionMode.BUCKET)
            return false;
        return true;
    }

    public void render(RenderGuiEvent.Post event) {
        if (!shouldRender())
            return;

        GuiGraphics context = event.getGuiGraphics();

        List<FluidSlotData> fluids = tank.getUniqueFluids();

        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();


        int handXOffset = this.arm == HumanoidArm.LEFT ? -169 : 118;
        if (client.player.getMainArm() == HumanoidArm.LEFT)
            handXOffset += 29;

        if (fluids.size() == 0) {
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 + handXOffset, scaledHeight - 22, 0, 0, 22, 22);
        } else if (selectedSlot == -1 || selectedSlot == fluids.size() - 1) {
            boolean isLeft = selectedSlot >= 0;
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 - (isLeft ? 20 : 0) + handXOffset, scaledHeight - 22, 22, 0, 42, 22);
        } else {
            context.blit(WIDGET_TEXTURE,
                    scaledWidth / 2 - 20 + handXOffset, scaledHeight - 22, 64, 0, 62, 22);
        }
        for (int i = -1; i <= 1; ++i) {
            int index = selectedSlot - i;
            if (index < -1 || index >= fluids.size())
                continue;
            int y = scaledHeight - 19;
            int x = scaledWidth / 2 - i * 20 + 3 + handXOffset;

            if (index == -1)
                context.renderItem(Items.BUCKET.getDefaultInstance(), x, y);

            else
                renderHotbarFluid(context, x, y, this.client.player, fluids.get(index), 0);

        }


        context.blit(WIDGET_TEXTURE,
                scaledWidth / 2 - 1 + handXOffset, scaledHeight - 22 - 1, 0, 22, 24, 22);
        matrixStack.popPose();

        RenderSystem.disableBlend();
    }

    private void renderHotbarFluid(GuiGraphics context, int x, int y, LocalPlayer player,
                                   FluidSlotData fluidSlotData, int i) {
        FluidRenderer.drawFluidInGui(context, fluidSlotData.fluidVariant(), x, y);
        FluidRenderer.drawFluidCount(client.font, context, fluidSlotData.amount(), x, y);
    }
}
