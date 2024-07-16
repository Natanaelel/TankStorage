package net.natte.tankstorage.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.natte.tankstorage.storage.InsertMode;
import net.natte.tankstorage.storage.TankInteractionMode;
import net.neoforged.neoforge.fluids.FluidType;

import java.text.NumberFormat;
import java.util.Locale;

public class Texts {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);

    public static final Component UNLINKED = Component.translatable("popup.tankstorage.unlinked");
    public static final Component FLUIDSLOT_HOVER_TOOLTIP = Component.translatable("tooltip.tankstorage.insert_or_extract_desc").withStyle(ChatFormatting.GRAY);

    private static final Component INTERACTIONMODE_OPEN_SCREEN = Component.translatable("popup.tankstorage.interactionmode.open_screen");
    private static final Component INTERACTIONMODE_BUCKET = Component.translatable("popup.tankstorage.interactionmode.bucket");

    public static Component interactionMode(TankInteractionMode interactionMode) {
        return switch (interactionMode) {
            case OPEN_SCREEN -> INTERACTIONMODE_OPEN_SCREEN;
            case BUCKET -> INTERACTIONMODE_BUCKET;
        };
    }

    private static final Component INSERTMODE_ALL_POPUP = Component.translatable("popup.tankstorage.insertmode.all");
    private static final Component INSERTMODE_FILTERED_POPUP = Component.translatable("popup.tankstorage.insertmode.filtered");
    private static final Component INSERTMODE_VOID_OVERFLOW_POPUP = Component.translatable("popup.tankstorage.insertmode.void_overflow");

    public static Component insertModePopup(InsertMode insertMode) {
        return switch (insertMode) {
            case ALL -> INSERTMODE_ALL_POPUP;
            case FILTERED -> INSERTMODE_FILTERED_POPUP;
            case VOID_OVERFLOW -> INSERTMODE_VOID_OVERFLOW_POPUP;
        };
    }

    private static final Component INSERTMODE_ALL = Component.translatable("title.tankstorage.insertmode.all").append("\n").append(Component.translatable("tooltip.tankstorage.insertmode.all").withStyle(ChatFormatting.DARK_GRAY));
    private static final Component INSERTMODE_FILTERED = Component.translatable("title.tankstorage.insertmode.filtered").append("\n").append(Component.translatable("tooltip.tankstorage.insertmode.filtered").withStyle(ChatFormatting.DARK_GRAY));
    private static final Component INSERTMODE_VOID_OVERFLOW = Component.translatable("title.tankstorage.insertmode.void_overflow").append("\n").append(Component.translatable("tooltip.tankstorage.insertmode.void_overflow").withStyle(ChatFormatting.DARK_GRAY));

    public static Component insertModeTooltip(InsertMode insertMode) {
        return switch (insertMode) {
            case ALL -> INSERTMODE_ALL;
            case FILTERED -> INSERTMODE_FILTERED;
            case VOID_OVERFLOW -> INSERTMODE_VOID_OVERFLOW;
        };
    }

    public static Component slotSizeTooltip(int slotSize) {
        String formattedSlotSize = NUMBER_FORMAT.format(slotSize / FluidType.BUCKET_VOLUME);

        return Component.translatable("tooltip.tankstorage.slotsize", formattedSlotSize);
    }

    public static Component slotCountTooltip(int slotCount) {
        return Component.translatable("tooltip.tankstorage.numslots", slotCount);
    }

}
