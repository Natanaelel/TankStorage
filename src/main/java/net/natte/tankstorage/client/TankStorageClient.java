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
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.events.MouseEvents;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.packet.server.OpenTankFromKeyBindPacketC2S;
import net.natte.tankstorage.packet.server.RequestTankPacketC2S;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.rendering.HudRenderer;
import net.natte.tankstorage.rendering.TankDockBlockEntityRenderer;
import net.natte.tankstorage.tooltip.TankTooltipComponent;
import net.natte.tankstorage.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.Set;
import java.util.UUID;

@Mod(value = TankStorage.MOD_ID, dist = Dist.CLIENT)
public class TankStorageClient {

    public static final KeyMapping lockSlotKeyBinding = ClientUtil.keyBind("lockslot", GLFW.GLFW_KEY_LEFT_ALT);
    public static final KeyMapping toggleInsertModeKeyBinding = ClientUtil.keyBind("toggleinsertmode");
    public static final KeyMapping toggleInteractionModeKeyBinding = ClientUtil.keyBind("toggleinteractionmode");
    public static final KeyMapping openTankFromKeyBinding = ClientUtil.keyBind("opentankfromkeybind");

    private static final HudRenderer tankHudRenderer = new HudRenderer();

    static {
        Util.isShiftDown = Screen::hasShiftDown;
    }

    public TankStorageClient(IEventBus modBus) {

        modBus.addListener(this::registerHandledScreens);
        modBus.addListener(this::registerItemColors);
        modBus.addListener(this::registerModelPredicates);
        registerRenderers(modBus);

        registerNetworkListeners();
        registerKeyBinds();
        registerKeyBindListeners();
        registerTooltipComponents();
        registerTickEvents();
        registerEvents();

    }

    private void registerHandledScreens(RegisterMenuScreensEvent event) {
        event.register(TankStorage.TANK_MENU.get(), TankScreen::new);
    }

    private void registerRenderers(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(tankHudRenderer::render);

        modBus.<EntityRenderersEvent.RegisterRenderers>addListener(event -> event.registerBlockEntityRenderer(TankStorage.TANK_DOCK_BLOCK_ENTITY.get(), TankDockBlockEntityRenderer::new));

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

    private void registerNetworkListeners() {
        ClientPlayNetworking.registerGlobalReceiver(SyncFluidPacketS2C.PACKET_TYPE, new SyncFluidPacketReceiver());
        ClientPlayNetworking.registerGlobalReceiver(TankPacketS2C.PACKET_TYPE, new TankPacketReceiver());
    }

    private void registerKeyBinds() {
        KeyBindingHelper.registerKeyBinding(lockSlotKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleInsertModeKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleInteractionModeKeyBinding);
        KeyBindingHelper.registerKeyBinding(openTankFromKeyBinding);

    }

    private void registerKeyBindListeners() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleInsertModeKeyBinding.wasPressed())
                ClientPlayNetworking.send(new ToggleInsertModePacketC2S());

            while (toggleInteractionModeKeyBinding.wasPressed()) {
                MouseEvents.onToggleInteractionMode(client.player, null);
            }

            while (openTankFromKeyBinding.wasPressed())
                ClientPlayNetworking.send(new OpenTankFromKeyBindPacketC2S());
        });
    }

    private void registerTooltipComponents() {
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof TankTooltipData tooltipData)
                return new TankTooltipComponent(tooltipData);
            return null;
        });
    }

    private void registerTickEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tankHudRenderer.tick(client);
            ClientTankCache.advanceThrottledQueue();
            sendQueuedRequests();
        });
    }

    private void registerEvents() {
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientTankCache.clear();
            tankHudRenderer.reset();
        });
    }

    private void sendQueuedRequests() {
        Set<UUID> requestQueue = ClientTankCache.getQueue();
        for (UUID uuid : requestQueue) {
            CachedFluidStorageState state = ClientTankCache.get(uuid);
            int revision = state == null ? -1 : state.getRevision();
            ClientPlayNetworking.send(new RequestTankPacketC2S(uuid, revision));
        }
        requestQueue.clear();
    }
}
