package net.kaupenjoe.tutorialmod.util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ModKeyBindings {
    public static KeyBinding zoomKey;
    public static KeyBinding veinMinerKey;

    public static void registerKeyBindings() {
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.zoom",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                KeyBinding.Category.MISC
        ));

        veinMinerKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.tutorialmod.veinminer",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                KeyBinding.Category.MISC
        ));
    }
}
