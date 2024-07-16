package net.natte.tankstorage;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.natte.tankstorage.block.TankDockBlock;
import net.natte.tankstorage.block.TankDockBlockEntity;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.item.TankLinkItem;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.packet.server.*;
import net.natte.tankstorage.recipe.TankLinkRecipe;
import net.natte.tankstorage.recipe.TankUpgradeRecipe;
import net.natte.tankstorage.menu.TankMenu;
import net.natte.tankstorage.menu.TankMenuFactory;
import net.natte.tankstorage.state.TankStateManager;
import net.natte.tankstorage.storage.TankOptions;
import net.natte.tankstorage.sync.SyncSubscriptionManager;
import net.natte.tankstorage.util.Util;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Mod(TankStorage.MOD_ID)
public class TankStorage {

    public static final String MOD_ID = "tankstorage";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int BUCKET = 1000;

    private static final TankType TANK_1 = new TankType("tank_1", 4 * BUCKET, 3, 1);
    private static final TankType TANK_2 = new TankType("tank_2", 16 * BUCKET, 6, 1);
    private static final TankType TANK_3 = new TankType("tank_3", 64 * BUCKET, 9, 1);
    private static final TankType TANK_4 = new TankType("tank_4", 256 * BUCKET, 6, 2);
    private static final TankType TANK_5 = new TankType("tank_5", 1024 * BUCKET, 5, 3);
    private static final TankType TANK_6 = new TankType("tank_6", 4096 * BUCKET, 9, 2);
    private static final TankType TANK_7 = new TankType("tank_7", 1_000_000 * BUCKET, 9, 3).itemProperty(Item.Properties::fireResistant);

    public static final TankType[] TANK_TYPES = {TANK_1, TANK_2, TANK_3, TANK_4, TANK_5, TANK_6, TANK_7};


    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MOD_ID);
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MOD_ID);


    public static final DeferredHolder<Item, TankLinkItem> TANK_LINK_ITEM = ITEMS.register("tank_link", () -> new TankLinkItem(new Item.Properties().stacksTo(1)));

    public static final DeferredHolder<Block, TankDockBlock> TANK_DOCK_BLOCK = BLOCKS.register("tank_dock", () -> new TankDockBlock(BlockBehaviour.Properties.of().strength(5.0f, 6.0f).mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().sound(SoundType.METAL).noOcclusion()));

    private static final DeferredHolder<Item, BlockItem> TANK_DOCK_ITEM = ITEMS.registerSimpleBlockItem(TANK_DOCK_BLOCK);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankDockBlockEntity>> TANK_DOCK_BLOCK_ENTITY = BLOCK_ENTITIES.register("tank_dock", () -> BlockEntityType.Builder.of(TankDockBlockEntity::new, TANK_DOCK_BLOCK.get()).build(null));


    public static final DeferredHolder<MenuType<?>, MenuType<TankMenu>> TANK_MENU = MENU_TYPES.register("tank_menu", () -> IMenuTypeExtension.create(TankMenuFactory::createClientScreenHandler));

    public static final DeferredHolder<RecipeSerializer<?>, TankUpgradeRecipe.Serializer> TANK_UPGRADE_RECIPE = RECIPES.register("tank_upgrade", TankUpgradeRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, TankLinkRecipe.Serializer> TANK_LINK_RECIPE = RECIPES.register("tank_link", TankLinkRecipe.Serializer::new);


    public static final DataComponentType<UUID> UUIDComponentType = DataComponentType.<UUID>builder().persistent(UUIDUtil.CODEC).networkSynchronized(UUIDUtil.STREAM_CODEC).build();
    public static final DataComponentType<TankOptions> OptionsComponentType = DataComponentType.<TankOptions>builder().persistent(TankOptions.CODEC).networkSynchronized(TankOptions.STREAM_CODEC).build();
    public static final DataComponentType<Integer> SelectedSlotComponentType = DataComponentType.<Integer>builder().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build();
    public static final DataComponentType<TankType> TankTypeComponentType = DataComponentType.<TankType>builder().persistent(TankType.CODEC).networkSynchronized(TankType.STREAM_CODEC).build();


    public TankStorage(IEventBus modBus) {

        ITEMS.register(modBus);
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENU_TYPES.register(modBus);
        RECIPES.register(modBus);
        COMPONENTS.register(modBus);

        registerTanks();
        registerComponents();

        modBus.addListener(this::registerCauldronInteractions);
        modBus.addListener(this::addItemsToCreativeTab);
        modBus.addListener(this::registerCapabilities);
        modBus.addListener(this::registerPackets);

        NeoForge.EVENT_BUS.addListener(TankStateManager::initialize);
        NeoForge.EVENT_BUS.addListener(SyncSubscriptionManager::tick);
    }


    private void registerCauldronInteractions(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CauldronInteraction.WATER.map().put(TANK_LINK_ITEM.get(), CauldronInteraction.DYED_ITEM);
            for (TankType type : TANK_TYPES)
                CauldronInteraction.WATER.map().put(type.getItem(), CauldronInteraction.DYED_ITEM);
        });
    }

    private void addItemsToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() != CreativeModeTabs.FUNCTIONAL_BLOCKS)
            return;

        for (TankType type : TANK_TYPES)
            event.accept(type.getItem());
        event.accept(TANK_LINK_ITEM.get());
        event.accept(TANK_DOCK_BLOCK.get());

    }

    private void registerTanks() {
        for (TankType tankType : TANK_TYPES) {
            tankType.register();
        }
    }

    private void registerComponents() {
        COMPONENTS.register("uuid", () -> UUIDComponentType);
        COMPONENTS.register("options", () -> OptionsComponentType);
        COMPONENTS.register("selected_slot", () -> SelectedSlotComponentType);
        COMPONENTS.register("type", () -> TankTypeComponentType);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TANK_DOCK_BLOCK_ENTITY.get(), TankDockBlockEntity::getFluidHandler);

        for (TankType type : TANK_TYPES)
            event.registerItem(Capabilities.FluidHandler.ITEM, Util::getFluidHandlerFromItem, type.getItem());
        event.registerItem(Capabilities.FluidHandler.ITEM, Util::getFluidHandlerFromItem, TANK_LINK_ITEM.get());
    }


    private void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToClient(SyncFluidPacketS2C.TYPE, SyncFluidPacketS2C.STREAM_CODEC, SyncFluidPacketS2C::receive);
        registrar.playToClient(TankPacketS2C.TYPE, TankPacketS2C.STREAM_CODEC, TankPacketS2C::receive);

        registrar.playToServer(LockSlotPacketC2S.TYPE, LockSlotPacketC2S.STREAM_CODEC, LockSlotPacketC2S::receive);
        registrar.playToServer(RequestTankPacketC2S.TYPE, RequestTankPacketC2S.STREAM_CODEC, RequestTankPacketC2S::receive);
        registrar.playToServer(UpdateTankOptionsPacketC2S.TYPE, UpdateTankOptionsPacketC2S.STREAM_CODEC, UpdateTankOptionsPacketC2S::receive);
        registrar.playToServer(SelectedSlotPacketC2S.TYPE, SelectedSlotPacketC2S.STREAM_CODEC, SelectedSlotPacketC2S::receive);
        registrar.playToServer(ToggleInsertModePacketC2S.TYPE, ToggleInsertModePacketC2S.STREAM_CODEC, ToggleInsertModePacketC2S::receive);
        registrar.playToServer(OpenTankFromKeyBindPacketC2S.TYPE, OpenTankFromKeyBindPacketC2S.STREAM_CODEC, OpenTankFromKeyBindPacketC2S::receive);
        registrar.playToServer(SyncSubscribePacketC2S.TYPE, SyncSubscribePacketC2S.STREAM_CODEC, SyncSubscribePacketC2S::receive);
    }
}
