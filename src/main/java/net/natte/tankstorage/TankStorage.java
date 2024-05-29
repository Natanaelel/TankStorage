package net.natte.tankstorage;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.natte.tankstorage.block.TankDockBlock;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.TankLinkItem;
import net.natte.tankstorage.packet.server.LockSlotPacketC2S;
import net.natte.tankstorage.packet.server.RequestTankPacketC2S;
import net.natte.tankstorage.state.TankStateManager;
import net.natte.tankstorage.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TankStorage implements ModInitializer {

	public static final String MOD_ID = "tankstorage";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final TankType TANK_1 = new TankType("tank_1", 4 * FluidConstants.BUCKET, 3, 1);
	private static final TankType TANK_2 = new TankType("tank_2", 16 * FluidConstants.BUCKET, 6, 1);
	private static final TankType TANK_3 = new TankType("tank_3", 64 * FluidConstants.BUCKET, 9, 1);
	private static final TankType TANK_4 = new TankType("tank_4", 256 * FluidConstants.BUCKET, 6, 2);
	private static final TankType TANK_5 = new TankType("tank_5", 1024 * FluidConstants.BUCKET, 5, 3);
	private static final TankType TANK_6 = new TankType("tank_6", 4096 * FluidConstants.BUCKET, 9, 2);
	private static final TankType TANK_7 = new TankType("tank_7", 1_000_000 * FluidConstants.BUCKET, 9, 3);

	public static final TankType[] TANK_TYPES = { TANK_1, TANK_2, TANK_3, TANK_4, TANK_5, TANK_6, TANK_7 };

	private static final Item TANK_LINK_ITEM = new TankLinkItem(new Item.Settings().maxCount(1));

	private static final Block TANK_DOCK_BLOCK = new TankDockBlock(
			FabricBlockSettings.create()
					.strength(5.0f, 6.0f)
					.mapColor(MapColor.BLACK)
					.requiresTool()
					.sounds(BlockSoundGroup.METAL).nonOpaque());

	public static final BlockEntityType<TankDockBlockEntity> TANK_DOCK_BLOCK_ENTITY = FabricBlockEntityTypeBuilder
			.create(TankDockBlockEntity::new, TANK_DOCK_BLOCK).build();

	@Override
	public void onInitialize() {

		registerTanks();
		registerLink();
		registerDock();

		registerNetworkListeners();
		registerEventListeners();
	}

	private void registerTanks() {
		for (TankType tankType : TANK_TYPES) {
			tankType.register();
		}

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(group -> {
			for (TankType type : TANK_TYPES) {
				group.add(type.getItem());
			}
			group.add(TANK_LINK_ITEM);
			group.add(TANK_DOCK_BLOCK);
		});
	}

	private void registerLink() {
		Registry.register(Registries.ITEM, Util.ID("tank_link"), TANK_LINK_ITEM);
	}

	private void registerDock() {

		Registry.register(Registries.BLOCK, Util.ID("tank_dock"), TANK_DOCK_BLOCK);
		Registry.register(Registries.ITEM, Util.ID("tank_dock"), new BlockItem(TANK_DOCK_BLOCK, new Item.Settings()));
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Util.ID("tank_dock"), TANK_DOCK_BLOCK_ENTITY);

		FluidStorage.SIDED.registerForBlockEntity(
				(tankDockBlockEntity, direction) -> tankDockBlockEntity.getFluidStorage(), TANK_DOCK_BLOCK_ENTITY);

	}

	private void registerNetworkListeners() {
		ServerPlayNetworking.registerGlobalReceiver(LockSlotPacketC2S.PACKET_TYPE, LockSlotPacketC2S::receive);
		ServerPlayNetworking.registerGlobalReceiver(RequestTankPacketC2S.PACKET_TYPE, RequestTankPacketC2S::receive);
	}

	private void registerEventListeners() {
		ServerLifecycleEvents.SERVER_STARTED.register(TankStateManager::initialize);
	}
}
