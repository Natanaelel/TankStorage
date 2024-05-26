package net.natte.tankstorage;

import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.packet.screenHandler.SyncFluidPacketS2C;
import net.natte.tankstorage.packetreceivers.SyncFluidPacketReceiver;
import net.natte.tankstorage.rendering.TankDockBlockEntityRenderer;
import net.natte.tankstorage.screen.TankScreen;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;

public class TankStorageClient implements ClientModInitializer {

	public static final KeyBinding lockSlotKeyBinding = new KeyBinding("key.tankstorage.lockslot",
			GLFW.GLFW_KEY_LEFT_ALT, "category.tankstorage");

	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerRenderers();
		registerNetworkListeners();
		registerKeyBinds();

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
	}

	private void registerNetworkListeners() {
		ClientPlayNetworking.registerGlobalReceiver(SyncFluidPacketS2C.PACKET_TYPE, new SyncFluidPacketReceiver());
	}

	private void registerKeyBinds() {
		KeyBindingHelper.registerKeyBinding(lockSlotKeyBinding);
	}

}
