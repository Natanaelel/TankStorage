package net.natte.tankstorage.client.events;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
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

    public static void onScroll(InputEvent.MouseScrollingEvent event){
        int scroll = -(int) Math.signum(event.getScrollDeltaY());

        LocalPlayer player = Minecraft.getInstance().player;
        if (!player.isShiftKeyDown())
            return;

        ItemStack right = player.getMainHandItem();
        ItemStack left = player.getOffhandItem();
        ItemStack tank;
        if (Util.isTankLike(right) && Util.hasUUID(right))
            tank = right;
        else if (Util.isTankLike(left) && Util.hasUUID(left))
            tank = left;
        else
            return;

        if (!isBucketMode(tank))
            return;

        CachedFluidStorageState state = ClientTankCache.get(Util.getUUID(tank));
        if (state == null)
            return;
        TankOptions options = Util.getOrCreateOptions(tank);

        int selectedSlot = options.selectedSlot;
        selectedSlot -= (int) Math.signum(scroll);
        selectedSlot = MathHelper.clamp(selectedSlot, -1, state.getUniqueFluids().size() - 1);
        options.selectedSlot = selectedSlot;
//        TODO
        PacketDistributor.sendToServer(new UpdateTankOptionsPacketC2S(options));

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
        PacketDistributor.sendToServer(new UpdateTankOptionsPacketC2S(Util.getOrCreateOptions(tankItem)));

        player.displayClientMessage(Component.translatable("popup.tankstorage.interactionmode."
                + interactionMode.toString().toLowerCase()), true);
    }

    private static boolean isBucketMode(ItemStack itemStack) {
        return Util.getInteractionMode(itemStack) == TankInteractionMode.BUCKET;
    }
}
