package net.natte.tankstorage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import net.minecraft.client.Mouse;
import net.minecraft.entity.player.PlayerInventory;
import net.natte.tankstorage.events.MouseEvents;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @WrapWithCondition(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;scrollInHotbar(D)V"))
    public boolean onScroll(PlayerInventory playerInventory, double scroll) {
        boolean bypassesVanilla = MouseEvents.onScroll(playerInventory, scroll);
        return !bypassesVanilla;
    }
}
