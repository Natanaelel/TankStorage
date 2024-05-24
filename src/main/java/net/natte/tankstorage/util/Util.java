package net.natte.tankstorage.util;

import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.TankItem;
import net.natte.tankstorage.item.TankLinkItem;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.state.TankPersistentState;
import net.natte.tankstorage.state.TankStateManager;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankOptions;

public class Util {

    public static Supplier<Boolean> isShiftDown = () -> false;

    public static Identifier ID(String id) {
        return new Identifier(TankStorage.MOD_ID, id);
    }

    public static TankFluidStorageState getOrCreateFluidStorage(ItemStack tankItem, World world) {
        if (!hasUUID(tankItem))
            setUUID(tankItem, UUID.randomUUID());
        UUID uuid = getUUID(tankItem);
        TankFluidStorageState tank = getFluidStorage(uuid, world);
        if (tank == null) {
            tank = TankFluidStorageState.create(getType(tankItem), uuid);
            TankStateManager.getState(world.getServer()).set(uuid, tank);
        }
        return tank;
    }

    // assumes stack has uuid
    @Nullable
    public static TankFluidStorageState getFluidStorage(ItemStack tankItem, World world) {
        return getFluidStorage(getUUID(tankItem), world);
    }

    @Nullable
    public static TankFluidStorageState getFluidStorage(UUID uuid, World world) {
        TankPersistentState state = TankStateManager.getState(world.getServer());
        return state.get(uuid);
    }

    public static boolean hasUUID(ItemStack tankItem) {
        if (!tankItem.hasNbt())
            return false;
        if (!tankItem.getNbt().containsUuid("tankstorage:uuid"))
            return false;
        return true;
    }

    public static UUID getUUID(ItemStack tankItem) {
        return tankItem.getNbt().getUuid("tankstorage:uuid");
    }

    public static void setUUID(ItemStack tankItem, UUID uuid) {
        tankItem.getOrCreateNbt().putUuid("tankstorage:uuid", uuid);
    }

    public static TankOptions getOrCreateOptions(ItemStack tankItem) {
        if (!tankItem.getOrCreateNbt().contains("tankstorage:options"))
            tankItem.getNbt().put("tankstorage:options", new TankOptions().asNbt());
        return TankOptions.fromNbt(tankItem.getNbt().getCompound("tankstorage:options"));
    }

    public static InsertMode getInsertMode(ItemStack tankItem) {
        return getOrCreateOptions(tankItem).insertMode;
    }

    private static TankType getType(ItemStack stack) {
        Item item = stack.getItem();

        if (item instanceof TankItem tankItem)
            return tankItem.type;

        if (item instanceof TankLinkItem)
            return TankType.fromName(stack.getNbt().getString("tankstorage:type"));

        return null;
    }

    public static boolean isTankLike(ItemStack stack){
        return stack.getItem() instanceof TankItem || stack.getItem() instanceof TankLinkItem;
    }
    

}
