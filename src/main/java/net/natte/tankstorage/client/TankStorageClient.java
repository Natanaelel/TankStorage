package net.natte.tankstorage.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.DyedItemColor;
import net.natte.tankstorage.TankStorage;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.client.rendering.HudRenderer;
import net.natte.tankstorage.client.rendering.TankDockBlockEntityRenderer;
import net.natte.tankstorage.client.screen.TankScreen;
import net.natte.tankstorage.client.tooltip.TankTooltipComponent;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.client.events.MouseEvents;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.packet.server.OpenTankFromKeyBindPacketC2S;
import net.natte.tankstorage.packet.server.RequestTankPacketC2S;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.UUID;

@Mod(value = TankStorage.MOD_ID, dist = Dist.CLIENT)
public class TankStorageClient {

    public static final KeyMapping lockSlotKeyBinding = ClientUtil.keyBind("lockslot", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping toggleInsertModeKeyBinding = ClientUtil.keyBind("toggleinsertmode");
    public static final KeyMapping toggleInteractionModeKeyBinding = ClientUtil.keyBind("toggleinteractionmode");
    public static final KeyMapping openTankFromKeyBinding = ClientUtil.keyBind("opentankfromkeybind");

    public static final HudRenderer tankHudRenderer = new HudRenderer();

    static {
        Util.isShiftDown = Screen::hasShiftDown;
    }

    public TankStorageClient(IEventBus modBus) {

        modBus.addListener(this::registerHandledScreens);
        modBus.addListener(this::registerItemColors);
        modBus.addListener(this::registerModelPredicates);
        modBus.addListener(this::registerKeyBinds);
        modBus.addListener(this::registerTooltipComponents);
        modBus.addListener(this::registerRenderers);
        modBus.addListener(this::initializeClientOnRenderThread);

        NeoForge.EVENT_BUS.addListener(this::handleTickEvents);
        NeoForge.EVENT_BUS.addListener(tankHudRenderer::render);
        NeoForge.EVENT_BUS.addListener(MouseEvents::onScroll);
    }


    private void registerHandledScreens(RegisterMenuScreensEvent event) {
        event.register(TankStorage.TANK_MENU.get(), TankScreen::new);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TankStorage.TANK_DOCK_BLOCK_ENTITY.get(), TankDockBlockEntityRenderer::new);
    }

    private void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (TankType type : TankStorage.TANK_TYPES) {
            event.register((stack, tintIndex) -> DyedItemColor.getOrDefault(stack, 0), type.getItem());
        }
        event.register((stack, tintIndex) -> DyedItemColor.getOrDefault(stack, 0), TankStorage.TANK_LINK_ITEM.get());
    }

    private void registerModelPredicates(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (TankType type : TankStorage.TANK_TYPES) {
                ItemProperties.register(type.getItem(), ResourceLocation.withDefaultNamespace("has_color"), (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1 : 0);
            }
            ItemProperties.register(TankStorage.TANK_LINK_ITEM.get(), ResourceLocation.withDefaultNamespace("has_color"), (stack, level, entity, seed) -> stack.has(DataComponents.DYED_COLOR) ? 1 : 0);

        });
    }

    private void initializeClientOnRenderThread(FMLClientSetupEvent event){
        event.enqueueWork(() -> Util.isClient.set(true));
    }

    public void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(toggleInteractionModeKeyBinding);
        event.register(toggleInsertModeKeyBinding);
        event.register(lockSlotKeyBinding);
        event.register(openTankFromKeyBinding);
    }

    private void handleTickEvents(ClientTickEvent.Post event) {
        handleInputs();
        tankHudRenderer.tick();
        ClientTankCache.advanceThrottledQueue();
        sendQueuedRequests();

    }

    private void handleInputs() {
        while (toggleInsertModeKeyBinding.consumeClick())
            PacketDistributor.sendToServer(ToggleInsertModePacketC2S.INSTANCE);

        while (toggleInteractionModeKeyBinding.consumeClick())
            MouseEvents.onToggleInteractionMode();


        while (openTankFromKeyBinding.consumeClick())
            PacketDistributor.sendToServer(OpenTankFromKeyBindPacketC2S.INSTANCE);
    }


    private void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(TankTooltipData.class, TankTooltipComponent::new);
    }

    private void sendQueuedRequests() {
        Set<UUID> requestQueue = ClientTankCache.getQueue();
        for (UUID uuid : requestQueue) {
            CachedFluidStorageState state = ClientTankCache.get(uuid);
            int revision = state == null ? -1 : state.getRevision();
            PacketDistributor.sendToServer(new RequestTankPacketC2S(uuid, revision));
        }
        requestQueue.clear();
    }
}
