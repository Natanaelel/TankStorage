package net.natte.tankstorage.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.natte.tankstorage.container.TankType;
import net.natte.tankstorage.gui.FluidSlot;
import net.natte.tankstorage.screenhandler.TankScreenHandler;
import net.natte.tankstorage.util.Util;

public class TankScreen extends HandledScreen<TankScreenHandler> {

    private static final Identifier WIDGETS_TEXTURE = Util.ID("textures/gui/widgets.png");

    private TankType type;
    private Identifier texture;

    public TankScreen(TankScreenHandler handler, PlayerInventory inventory, Text title, TankType type) {
        super(handler, inventory, title);
        this.type = type;

        this.texture = Util.ID("textures/gui/" + this.type.width() + "x" + this.type.height() + ".png");
        this.backgroundHeight = 114 + this.type.height() * 18;
        this.playerInventoryTitleY += this.type.height() * 18 - 52;
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext drawContext, float timeDelta, int mouseX, int mouseY) {

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(this.texture, x, y, 0, 0, backgroundWidth, backgroundHeight,
                (int) Math.ceil(backgroundWidth / 256d) * 256, (int) Math.ceil(backgroundHeight / 256d) * 256);

    }

    @Override
    protected void drawSlot(DrawContext context, Slot slot) {
        if (!(slot instanceof FluidSlot fluidSlot)) {
            super.drawSlot(context, slot);
            return;
        }

        context.drawTexture(WIDGETS_TEXTURE, fluidSlot.x-1, fluidSlot.y-1, 1, 149, 18, 18);

        if(true){
            context.drawTexture(WIDGETS_TEXTURE, fluidSlot.x, fluidSlot.y, 0, 46, 16, 16);
        }
        


    }

}
