package net.natte.tankstorage;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.rendering.TankDockBlockEntityRenderer;
import net.natte.tankstorage.screen.TankScreen;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;

public class TankStorageClient implements ClientModInitializer {


	static {
		Util.isShiftDown = () -> Screen.hasShiftDown();
	}

	@Override
	public void onInitializeClient() {

		registerHandledScreens();
		registerRenderers();

	}

	private void registerHandledScreens() {
		for (TankType type : TankStorage.TANK_TYPES) {
			HandledScreens.<TankScreenHandler, TankScreen>register(type.getScreenhandlerType(), (screenhandler, playerInventory, title) -> {
				return new TankScreen(screenhandler, playerInventory, title, type);
			});
		}
	}

	private void registerRenderers() {
		BlockEntityRendererFactories.register(TankStorage.TANK_DOCK_BLOCK_ENTITY, TankDockBlockEntityRenderer::new);
	}

}
