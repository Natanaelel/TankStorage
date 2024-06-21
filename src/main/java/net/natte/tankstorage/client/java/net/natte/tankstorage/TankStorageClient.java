package net.natte.tankstorage;

import java.util.Set;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.cache.CachedFluidStorageState;
import net.natte.tankstorage.cache.ClientTankCache;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.events.MouseEvents;
import net.natte.tankstorage.item.tooltip.TankTooltipData;
import net.natte.tankstorage.packet.client.TankPacketS2C;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.packet.server.OpenTankFromKeyBindPacketC2S;
import net.natte.tankstorage.packet.server.RequestTankPacketC2S;
import net.natte.tankstorage.packetreceivers.SyncFluidPacketReceiver;
import net.natte.tankstorage.packetreceivers.TankPacketReceiver;
import net.natte.tankstorage.rendering.HudRenderer;
import net.natte.tankstorage.rendering.TankDockBlockEntityRenderer;
import net.natte.tankstorage.screen.TankScreen;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.tooltip.TankTooltipComponent;
import net.natte.tankstorage.util.Util;

public class TankStorageClient implements ClientModInitializer {

	public static final KeyBinding lockSlotKeyBinding = ClientUtil.keyBind("lockslot", GLFW.GLFW_KEY_LEFT_ALT);
	public static final KeyBinding toggleInsertModeKeyBinding = ClientUtil.keyBind("toggleinsertmode");
	public static final KeyBinding toggleInteractionModeKeyBinding = ClientUtil.keyBind("toggleinteractionmode");
	public static final KeyBinding openTankFromKeyBinding = ClientUtil.keyBind("opentankfromkeybind");

	private static final HudRenderer tankHudRenderer = new HudRenderer();

	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerRenderers();
		registerItemColors();
		registerModelPredicates();
		registerNetworkListeners();
		registerKeyBinds();
		registerKeyBindListeners();
		registerTooltipComponents();
		registerTickEvents();
		registerEvents();

	}

	private void registerHandledScreens() {
		for (TankType type : TankStorage.TANK_TYPES) {
			HandledScreens.<TankScreenHandler, TankScreen>register(type.getScreenhandlerType(),
					(screenhandler, playerInventory, title) -> {
						return new TankScreen(screenhandler, playerInventory, title, type);
					});
		}
	}

	private void registerRenderers() {
		BlockEntityRendererFactories.register(TankStorage.TANK_DOCK_BLOCK_ENTITY, TankDockBlockEntityRenderer::new);

		HudRenderCallback.EVENT.register(tankHudRenderer::render);
	}

	private void registerItemColors() {
		for (TankType type : TankStorage.TANK_TYPES) {
			ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
				return ((DyeableItem) stack.getItem()).getColor(stack);
			}, new ItemConvertible[] { type.getItem() });
		}

		ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
			return ((DyeableItem) stack.getItem()).getColor(stack);
		}, new ItemConvertible[] { TankStorage.TANK_LINK_ITEM });
	}

	private void registerModelPredicates() {
		for (TankType type : TankStorage.TANK_TYPES) {
			ModelPredicateProviderRegistry.register(type.getItem(), new Identifier("has_color"),
					(itemStack, clientWorld, livingEntity, i) -> {
						return ((DyeableItem) itemStack.getItem()).hasColor(itemStack) ? 1.0f : 0.0f;
					});
		}

		ModelPredicateProviderRegistry.register(TankStorage.TANK_LINK_ITEM, new Identifier("has_color"),
				(itemStack, clientWorld, livingEntity, i) -> {
					return ((DyeableItem) itemStack.getItem()).hasColor(itemStack) ? 1.0f : 0.0f;
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
