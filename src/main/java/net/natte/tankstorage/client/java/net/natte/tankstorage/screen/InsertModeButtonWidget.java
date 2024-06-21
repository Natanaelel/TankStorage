package net.natte.tankstorage.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.packet.server.ToggleInsertModePacketC2S;
import net.natte.tankstorage.storage.InsertMode;

@Environment(EnvType.CLIENT)
public class InsertModeButtonWidget extends TexturedButtonWidget {

    public InsertModeOption state;

    private static final int textureWidth = 256;
    private static final int textureHeight = 256;

    public InsertModeButtonWidget(InsertMode insertMode, int x, int y, int width, int height, int hoveredVOffset,
            Identifier texture) {
        super(x, y, width, height, 0, 0, hoveredVOffset, texture, textureWidth, textureHeight,
                b -> onInsertModeButtonPress((InsertModeButtonWidget) b), ScreenTexts.EMPTY);

        this.state = InsertModeOption.from(insertMode);
        this.refreshTooltip();
        this.setTooltipDelay(700);
    }

    @Override
    public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawTexture(context, this.texture, this.getX(), this.getY(), this.state.uOffset(), this.state.vOffset(),
                this.hoveredVOffset,
                this.width, this.height, textureWidth, textureHeight);
    }

    public void refreshTooltip() {
        this.setTooltip(state.getTooltip());
    }

    private static void onInsertModeButtonPress(InsertModeButtonWidget button) {
        button.state = InsertModeOption.from(button.state.toInsertMode().next());
        button.refreshTooltip();
        ClientPlayNetworking.send(new ToggleInsertModePacketC2S());
    }
}

enum InsertModeOption {
    ALL("all", 14, 70),
    FILTERED("filtered", 28, 70),
    VOID_OVERFLOW("void_overflow", 42, 70);

    private Text name;
    private Text info;

    private int uOffset;
    private int vOffset;

    private InsertModeOption(String name, int uOffset, int vOffset) {
        this.name = Text.translatable("title.tankstorage.insertmode." + name);
        this.info = Text.translatable("tooltip.tankstorage.insertmode." + name);
        this.uOffset = uOffset;
        this.vOffset = vOffset;
    }

    public static InsertModeOption from(InsertMode insertMode) {
        return switch (insertMode) {
            case ALL -> ALL;
            case FILTERED -> FILTERED;
            case VOID_OVERFLOW -> VOID_OVERFLOW;
        };
    }

    public InsertMode toInsertMode() {
        return switch (this) {
            case ALL -> InsertMode.ALL;
            case FILTERED -> InsertMode.FILTERED;
            case VOID_OVERFLOW -> InsertMode.VOID_OVERFLOW;
        };
    }

    public Text getName() {
        return this.name;
    }

    public Text getInfo() {
        return this.info;
    }

    public int uOffset() {
        return this.uOffset;
    }

    public int vOffset() {
        return this.vOffset;
    }

    public Tooltip getTooltip() {
        return Tooltip.of(
                getName().copy().append(Text.empty().append("\n").append(getInfo()).formatted(Formatting.DARK_GRAY)));
    }
}
