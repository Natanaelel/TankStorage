package net.natte.tankstorage;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.option.KeyBinding;

public class ClientUtil {
    public static KeyBinding keyBind(String id) {
        return keyBind(id, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static KeyBinding keyBind(String id, int defaultKey) {
        return new KeyBinding("key.tankstorage." + id, defaultKey, "category.tankstorage");
    }
}
