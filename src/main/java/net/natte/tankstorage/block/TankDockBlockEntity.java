package net.natte.tankstorage.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.storage.TankFluidHandler;
import net.natte.tankstorage.util.Util;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class TankDockBlockEntity extends BlockEntity {

    private static final String TANK_ITEM_KEY = "tankstorage:tank";
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
        this.setChanged();
        return tankItem;
    }

    public void putTank(ItemStack stack) {
        this.tankItem = stack;
        setChanged();
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction ignored) {
        if (this.fluidHandler == null)
            this.fluidHandler = createFluidHandler();

        return this.fluidHandler;
    }

    @Nullable
    private TankFluidHandler createFluidHandler() {
        if (level == null)
            return null;
        if (level.isClientSide)
            return null;
        if (!hasTank())
            return null;
        return Util.getFluidHandlerFromItem(tankItem);
    }

    @Override
    public void setChanged() {
        fluidHandler = null;
        invalidateCapabilities();
        super.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.saveAdditional(nbt, registryLookup);
        Tag itemAsNbt = this.tankItem.saveOptional(registryLookup);
        nbt.put(TANK_ITEM_KEY, itemAsNbt);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        super.loadAdditional(nbt, registryLookup);
        this.tankItem = ItemStack.parseOptional(registryLookup, nbt.getCompound(TANK_ITEM_KEY));
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }
}
