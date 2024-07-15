package net.natte.tankstorage.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.util.Texts;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Duration;

public class InsertModeButtonWidget extends Button {

    public InsertMode insertMode;

    private final ResourceLocation texture;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    private int uOffset;

    public InsertModeButtonWidget(InsertMode insertMode, int x, int y, int width, int height, int hoveredVOffset,
                                  ResourceLocation texture) {
        super(x, y, width, height, CommonComponents.EMPTY, b -> onInsertModeButtonPress((InsertModeButtonWidget) b), DEFAULT_NARRATION);
        this.texture = texture;

        this.insertMode = insertMode;

        this.refreshTooltip();
        updateUOffset();
        this.setTooltipDelay(Duration.ofMillis(700));
    }


    @Override
    public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
        context.blit(this.texture, this.getX(), this.getY(), uOffset, 70 + (this.isHoveredOrFocused() ? this.height : 0),
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip() {
        this.setTooltip(
                Tooltip.create(Texts.insertModeTitle(this.insertMode).copy()
                        .append("\n")
                        .append(Texts.insertModeTooltip(this.insertMode).copy()
                                .withStyle(ChatFormatting.DARK_GRAY)
                        )));
    }

    private static void onInsertModeButtonPress(InsertModeButtonWidget button) {
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
