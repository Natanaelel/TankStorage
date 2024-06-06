package net.natte.tankstorage.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.packet.server.UpdateTankOptionsPacketC2S;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.util.Util;

@Environment(EnvType.CLIENT)
public class MouseEvents {

    public static boolean onScroll(PlayerInventory playerInventory, double scroll) {

        PlayerEntity player = playerInventory.player;

        if (!player.isSneaking())
            return false;

        ItemStack right = player.getMainHandStack();
        ItemStack left = player.getOffHandStack();
        ItemStack tank;
        if (Util.isTankLike(right) && Util.hasUUID(right))
            tank = right;
        else if (Util.isTankLike(left) && Util.hasUUID(left))
            tank = left;
        else
            return false;

        if (!isBucketMode(tank))
            return false;

        CachedFluidStorageState state = ClientTankCache.get(Util.getUUID(tank));
        if (state == null)
            return false;
        TankOptions options = Util.getOrCreateOptions(tank);

        int selectedSlot = options.selectedSlot;
        selectedSlot -= (int) Math.signum(scroll);
        selectedSlot = MathHelper.clamp(selectedSlot, -1, state.getNonEmptyFluids().size() - 1);
        options.selectedSlot = selectedSlot;
        ClientPlayNetworking.send(new UpdateTankOptionsPacketC2S(options));

        return true;

    }

    public static void onToggleInteractionMode(PlayerEntity player, ItemStack tankItem) {

        if (tankItem == null)
            tankItem = Util.getHeldTank(player);
        if (tankItem == null)
            return;

        TankOptions options = Util.getOrCreateOptions(tankItem);
        options.interactionMode = options.interactionMode.next();

        ClientPlayNetworking.send(new UpdateTankOptionsPacketC2S(options));

        player.sendMessage(Text.translatable("popup.tankstorage.interactionmode."
                + options.interactionMode.toString().toLowerCase()), true);

    }

    private static boolean isBucketMode(ItemStack itemStack) {
        return Util.getInteractionMode(itemStack) == TankInteractionMode.BUCKET;
    }
}