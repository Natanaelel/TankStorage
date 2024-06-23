package net.natte.tankstorage.client.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.client.TankStorageClient;
import net.natte.tankstorage.client.rendering.HudRenderer;
import net.natte.tankstorage.packet.server.SelectedSlotPacketC2S;
import net.natte.tankstorage.packet.server.UpdateTankOptionsPacketC2S;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@OnlyIn(Dist.CLIENT)
public class MouseEvents {

    public static void onScroll(InputEvent.MouseScrollingEvent event) {

        int scroll = -(int) Math.signum(event.getScrollDeltaY());

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null)
            return;

        if (!player.isShiftKeyDown())
            return;

        HudRenderer preview = TankStorageClient.tankHudRenderer;

        if (!preview.isBucketMode())
            return;

        CachedFluidStorageState cachedBankStorage = preview.tank;

        if (cachedBankStorage == null)
            return;


        int selectedItemSlot = preview.selectedSlot;

        int newSelectedItemSlot = Mth.clamp(selectedItemSlot + scroll, 0, cachedBankStorage.getUniqueFluids().size() - 1);
        preview.selectedSlot = newSelectedItemSlot;

        PacketDistributor.sendToServer(new SelectedSlotPacketC2S(preview.renderingFromHand == InteractionHand.MAIN_HAND, newSelectedItemSlot));

        event.setCanceled(true);
    }


    public static void onToggleInteractionMode() {

        Player player = Minecraft.getInstance().player;

        ItemStack tankItem = Util.getHeldTank(player);

        if (tankItem == null)
            return;

        tankItem.update(TankStorage.OptionsComponentType, TankOptions.DEFAULT, TankOptions::nextInteractionMode);
        TankInteractionMode interactionMode = Util.getInteractionMode(tankItem);

        // TODO: make more robust
        PacketDistributor.sendToServer(new UpdateTankOptionsPacketC2S(Util.getOptionsOrDefault(tankItem)));

        player.displayClientMessage(Component.translatable("popup.tankstorage.interactionmode."
                + interactionMode.toString().toLowerCase()), true);
    }
}
