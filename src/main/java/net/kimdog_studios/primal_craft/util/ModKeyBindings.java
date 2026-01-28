package net.kimdog_studios.primal_craft.util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyBinding zoomKey;
    public static KeyBinding veinMinerKey;
    public static KeyBinding configMenuKey;

    public static void registerKeyBindings() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.primal-craft.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KeyBinding.Category.MISC
        ));

        veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.primal-craft.veinminer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.MISC
        ));

        configMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.primal-craft.config_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                KeyBinding.Category.MISC
        ));
    }
}
