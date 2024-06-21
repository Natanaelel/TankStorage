package net.natte.tankstorage.client;

import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class ClientUtil {
    public static KeyMapping keyBind(String id) {
        return keyBind(id, GLFW.GLFW_KEY_UNKNOWN);
    }

    public static KeyMapping keyBind(String id, int defaultKey) {
        return new KeyMapping("key.tankstorage." + id, defaultKey, "category.tankstorage");
    }
}
