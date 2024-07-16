package net.natte.tankstorage.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.util.Texts;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;

public class InsertModeButtonWidget extends Button {

    public InsertMode insertMode;

    private final ResourceLocation texture;

    private int uOffset;

    public InsertModeButtonWidget(InsertMode insertMode, int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, CommonComponents.EMPTY, b -> onPress((InsertModeButtonWidget) b), DEFAULT_NARRATION);
        this.texture = texture;

        this.insertMode = insertMode;

        this.refreshTooltip();
        updateUOffset();
        this.setTooltipDelay(Duration.ofMillis(700));
    }

    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture, this.getX(), this.getY(), uOffset, 70 + (this.isHoveredOrFocused() ? this.height : 0), this.width, this.height);
    }

    public void refreshTooltip() {
        this.setTooltip(Tooltip.create(Texts.insertModeTooltip(this.insertMode)));
    }

    private static void onPress(InsertModeButtonWidget button) {
        button.insertMode = button.insertMode.next();
        button.updateUOffset();
        button.refreshTooltip();
        PacketDistributor.sendToServer(new ToggleInsertModePacketC2S());
    }

    private void updateUOffset() {
        this.uOffset = switch (this.insertMode) {
            case ALL -> 0;
            case FILTERED -> 14;
            case VOID_OVERFLOW -> 28;
        };
    }
}
