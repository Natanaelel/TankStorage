package net.natte.tankstorage.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.packet.server.SyncSubscribePacketC2S;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.LargeFluidSlotData;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class HudRenderer {

    private static final ResourceLocation WIDGET_TEXTURE = Util.ID("textures/gui/widgets.png");

    private Minecraft client;

    private UUID uuid;
    public CachedFluidStorageState tank;
    private TankOptions options;
    public int selectedSlot = -1;

    private HumanoidArm arm;
    private boolean hasTank = false;
    public InteractionHand renderingFromHand;
    private ItemStack tankItem = ItemStack.EMPTY;
    private HumanoidArm mainArm;
    private short uniqueId = 0;

    public void tick() {
        if (this.client == null)
            this.client = Minecraft.getInstance();

        if (client.player == null)
            return;


        if (didHeldTankChange())
            updateTank();
        if (hasTank)
            tickTank();
    }

    private void updateTank() {
        if (this.hasTank) {
            PacketDistributor.sendToServer(SyncSubscribePacketC2S.unsubscribe(this.uuid));
        }

        @Nullable InteractionHand handWithTank = getHandToRenderFrom();
        if (handWithTank != null) {
            this.hasTank = true;
            this.tankItem = client.player.getItemInHand(handWithTank);
            this.uuid = Util.getUUID(tankItem);
            this.renderingFromHand = handWithTank;
            this.mainArm = this.client.player.getMainArm();
            this.arm = this.renderingFromHand == InteractionHand.MAIN_HAND ? mainArm : mainArm.getOpposite();
            this.options = Util.getOptionsOrDefault(this.tankItem);
            this.uniqueId = options.uniqueId();
            this.selectedSlot = this.tankItem.getOrDefault(TankStorage.SelectedSlotComponentType, 0);
            this.tank = ClientTankCache.getAndQueueThrottledUpdate(this.uuid, 20);
            if (this.tank != null)
                this.selectedSlot = Mth.clamp(this.selectedSlot, -1, this.tank.getUniqueFluids().size() - 1);

            PacketDistributor.sendToServer(SyncSubscribePacketC2S.subscribe(this.uuid));
        } else {
            this.hasTank = false;
        }
    }

    private void tickTank() {
        if (ClientTankCache.markDirtyForPreview) {
            ClientTankCache.markDirtyForPreview = false;
            this.tank = ClientTankCache.get(uuid);
            if (this.tank != null) {
                this.selectedSlot = Mth.clamp(this.selectedSlot, -1, this.tank.getUniqueFluids().size() - 1);
            }
        }
    }

    private boolean shouldTryRenderFrom(ItemStack stack) {
        return Util.isTankLike(stack) && Util.hasUUID(stack) && Util.getInteractionMode(stack) == TankInteractionMode.BUCKET;
    }

    @Nullable
    private InteractionHand getHandToRenderFrom() {
        if (shouldTryRenderFrom(client.player.getMainHandItem()))
            return InteractionHand.MAIN_HAND;
        if (shouldTryRenderFrom(client.player.getOffhandItem()))
            return InteractionHand.OFF_HAND;
        return null;
    }

    private boolean didHeldTankChange() {
        @Nullable InteractionHand hand = getHandToRenderFrom();
        if (hand == null)
            return this.hasTank;
        if (!this.hasTank)
            return true;
        if (hand != renderingFromHand)
            return true;
        ItemStack tankItem = client.player.getItemInHand(hand);
        return Util.getUniqueId(tankItem) != this.uniqueId;
    }

    private boolean shouldRender() {
        return hasTank && tank != null;
    }

    public void render(RenderGuiEvent.Post event) {
        if (!shouldRender())
            return;

        GuiGraphics context = event.getGuiGraphics();

        List<LargeFluidSlotData> fluids = tank.getUniqueFluids();

        int scaledHeight = context.guiHeight();
        int scaledWidth = context.guiWidth();

        RenderSystem.enableBlend();

        PoseStack matrixStack = context.pose();
        matrixStack.pushPose();


        int handXOffset = this.arm == HumanoidArm.LEFT ? -169 : 118;
        if (mainArm == HumanoidArm.LEFT)
            handXOffset += 29;

        if (fluids.isEmpty()) {
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
                                   LargeFluidSlotData fluidSlotData, int i) {
        FluidRenderer.drawFluidInGui(context, fluidSlotData.fluid(), x, y, false);
        FluidRenderer.drawFluidCount(client.font, context, fluidSlotData.amount(), x, y);
    }

    public ItemStack getItem() {
        return tankItem;
    }

    public boolean isRendering() {
        return hasTank && tank != null;
    }

    public CachedFluidStorageState getFluidStorage() {
        return tank;
    }
}
