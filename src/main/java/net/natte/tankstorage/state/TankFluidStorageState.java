package net.natte.tankstorage.state;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankFluidStorage;
import net.natte.tankstorage.storage.TankSingleFluidStorage;

public class TankFluidStorageState {

    public TankType type;
    public UUID uuid;

    private List<TankSingleFluidStorage> fluidStorageParts;

    private TankFluidStorageState(TankType type, UUID uuid) {
        this.type = type;
        this.uuid = uuid;
    }
    
    public TankFluidStorage getFluidStorage(InsertMode insertMode){
        return new TankFluidStorage(fluidStorageParts, insertMode);
    }
    // server-side only
    public ExtendedScreenHandlerFactory getScreenHandlerFactory(ItemStack tankItem, ScreenHandlerContext screenHandlerContext){
        return new ExtendedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
                return new TankScreenHandler(syncId, playerInventory, type, TankFluidStorageState.this, tankItem, screenHandlerContext);
            }

            @Override
            public Text getDisplayName() {
                return tankItem.getName();
            }

            @Override
            public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                buf.writeItemStack(tankItem);
            }
            
        };
    }

    public ExtendedScreenHandlerFactory getScreenHandlerFactory(ItemStack tankItem){
        return getScreenHandlerFactory(tankItem, ScreenHandlerContext.EMPTY);
    }

    public TankFluidStorageState asType(TankType type) {
        if (this.type != type) {
            if (type.size() < this.type.size()) {
                return this;
            }
            return changeType(type);
        }
        return this;
    }



    public TankFluidStorageState changeType(TankType type) {
        TankStorage.LOGGER
                .info("Upgrading bank from " + this.type.getName() + " to " + type.getName() + " uuid " + this.uuid);

        TankFluidStorageState tank = new TankFluidStorageState(type, this.uuid);

        for (int i = 0; i < this.fluidStorageParts.size(); ++i) {
            TankSingleFluidStorage oldFluidStorage = tank.fluidStorageParts.get(i);

            tank.fluidStorageParts.set(i,
                    new TankSingleFluidStorage(type.getCapacity(), oldFluidStorage.getAmount(),
                            oldFluidStorage.getResource(), oldFluidStorage.isLocked()));
        }
        return tank;
    }

    public static TankFluidStorageState readNbt(NbtCompound nbt) {

        UUID uuid = nbt.getUuid("uuid");
        TankType type = TankType.fromName(nbt.getString("type"));

        List<TankSingleFluidStorage> parts = new ArrayList<>();
        NbtList fluids = nbt.getList("fluids", NbtCompound.LIST_TYPE);

        for (NbtElement nbtElement : fluids) {
            NbtCompound fluidNbt = (NbtCompound) nbtElement;

            FluidVariant fluidVariant = FluidVariant.fromNbt(fluidNbt.getCompound("variant"));
            long amount = fluidNbt.getLong("amount");
            boolean isLocked = fluidNbt.getBoolean("locked");

            TankSingleFluidStorage fluidSlot = new TankSingleFluidStorage(type.getCapacity(), amount, fluidVariant,
                    isLocked);
            parts.add(fluidSlot);
        }

        TankFluidStorageState state = new TankFluidStorageState(type, uuid);
        state.fluidStorageParts = parts;
        return state;
    }

    public static NbtCompound writeNbt(TankFluidStorageState tank) {

        NbtCompound nbt = new NbtCompound();

        nbt.putUuid("uuid", tank.uuid);
        nbt.putString("type", tank.type.getName());

        NbtList fluids = new NbtList();

        for (TankSingleFluidStorage part : tank.fluidStorageParts) {
            NbtCompound fluidNbt = new NbtCompound();
            fluidNbt.put("variant", part.getResource().toNbt());
            fluidNbt.putLong("amount", part.getAmount());
            fluidNbt.putBoolean("locked", part.isLocked());
            fluids.add(fluidNbt);
        }

        nbt.put("fluids", fluids);

        return nbt;
    }

    public static TankFluidStorageState create(TankType type, UUID uuid) {
        TankFluidStorageState tank = new TankFluidStorageState(type, uuid);
        List<TankSingleFluidStorage> fluidStorageParts = new ArrayList<>(type.size());
        for (int i = 0; i < type.size(); ++i) {
            fluidStorageParts.add(new TankSingleFluidStorage(type.getCapacity()));
        }
        tank.fluidStorageParts = fluidStorageParts;
        return tank;
    }
}
