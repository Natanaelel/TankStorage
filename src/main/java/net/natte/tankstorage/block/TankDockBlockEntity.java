package net.natte.tankstorage.block;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.util.Util;

public class TankDockBlockEntity extends BlockEntity {

    private ItemStack tankItem = ItemStack.EMPTY;
    private Storage<FluidVariant> fluidStorage = null;

    public TankDockBlockEntity(BlockPos pos, BlockState state) {
        super(TankStorage.TANK_DOCK_BLOCK_ENTITY, pos, state);
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
        if (fluidStorage != null)
            return fluidStorage;

        if (world.isClient)
            return Storage.empty();

        if (tankItem.isEmpty())
            return Storage.empty();

        if (!Util.hasUUID(tankItem))
            return Storage.empty();

        fluidStorage = Util.getOrCreateFluidStorage(tankItem, world).getFluidStorage(Util.getInsertMode(tankItem));

        return fluidStorage;
    }

    @Override
    public void markDirty() {
        fluidStorage = null;
        super.markDirty();
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
