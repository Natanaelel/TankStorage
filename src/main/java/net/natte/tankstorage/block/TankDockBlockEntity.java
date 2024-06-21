package net.natte.tankstorage.block;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.state.TankFluidStorageState;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class TankDockBlockEntity extends BlockEntity {

    private ItemStack tankItem = ItemStack.EMPTY;
    private TankFluidHandler fluidHandler = null;

    public TankDockBlockEntity(BlockPos pos, BlockState state) {
        super(TankStorage.TANK_DOCK_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean hasTank() {
        return !this.tankItem.isEmpty();
    }

    public ItemStack getTank() {
        return tankItem;
    }

    public ItemStack pickUpTank() {
        ItemStack tankItem = this.tankItem;
        this.tankItem = ItemStack.EMPTY;
        this.markDirty();
        return tankItem;
    }

    public void putTank(ItemStack stack) {
        this.tankItem = stack;
        markDirty();
    }

    public Storage<FluidVariant> getFluidStorage() {
        if (fluidHandler != null)
            return fluidHandler;

        if (world.isClient)
            return Storage.empty();

        if (tankItem.isEmpty())
            return Storage.empty();

        if (!Util.hasUUID(tankItem))
            return Storage.empty();

//        fluidHandler =

        return fluidHandler;
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction direction) {
        // TODO
        if(this.fluidHandler == null){
            TankFluidStorageState tank = Util.getOrCreateFluidStorage(tankItem);
            this.fluidHandler = tank.getFluidHandler(Util.getInsertMode(tankItem));;
        }

        return this.fluidHandler;
    }

    @Override
    public void setChanged() {
        fluidHandler = null;
        super.setChanged();
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        tankItem = ItemStack.fromNbt(nbt.getCompound("tankstorage:tank"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        NbtCompound itemAsNbt = new NbtCompound();
        tankItem.writeNbt(itemAsNbt);
        nbt.put("tankstorage:tank", itemAsNbt);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }


}
