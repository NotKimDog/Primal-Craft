package net.kaupenjoe.tutorialmod.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kaupenjoe.tutorialmod.network.TypingIndicatorPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.input.KeyInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @org.spongepowered.asm.mixin.Shadow protected net.minecraft.client.gui.widget.TextFieldWidget chatField;

    private boolean wasTyping = false;
    private long lastTypingUpdateMs = 0;
    private static final long TYPING_UPDATE_INTERVAL_MS = 100; // Throttle updates to every 100ms

    @Inject(method = "init", at = @At("TAIL"))
    private void onChatScreenOpen(CallbackInfo ci) {
        wasTyping = false;
        lastTypingUpdateMs = 0;
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onChatScreenClose(CallbackInfo ci) {
        // Notify server that player stopped typing when closing chat
        if (wasTyping && MinecraftClient.getInstance().player != null) {
            ClientPlayNetworking.send(new TypingIndicatorPayload(MinecraftClient.getInstance().player.getName().getString(), false, ""));
            wasTyping = false;
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"))
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || chatField == null) return;

        int keyCode = input.key();
        long currentTime = System.currentTimeMillis();

        // Check for printable character keys or common text input keys
        boolean isTextKey = (keyCode >= 48 && keyCode <= 57) ||   // Numbers 0-9
                           (keyCode >= 65 && keyCode <= 90) ||   // Letters A-Z
                           keyCode == 32 ||                       // Space
                           keyCode == 259 ||                      // Delete
                           keyCode == 261 ||                      // Period/Greater
                           keyCode == 188 ||                      // Comma/Less
                           keyCode == 47 ||                       // Slash
                           keyCode == 56 ||                       // Apostrophe
                           keyCode == 48 ||                       // Semicolon
                           keyCode == 61 ||                       // Equal
                           keyCode == 45 ||                       // Minus
                           keyCode == 91 ||                       // Left bracket
                           keyCode == 92 ||                       // Backslash
                           keyCode == 93;                         // Right bracket

        if (isTextKey || keyCode == 259) { // Text key or Backspace
            if (!wasTyping) {
                wasTyping = true;
                lastTypingUpdateMs = currentTime;
                ClientPlayNetworking.send(new TypingIndicatorPayload(client.player.getName().getString(), true, ""));
            } else if (currentTime - lastTypingUpdateMs >= TYPING_UPDATE_INTERVAL_MS) {
                // Throttled update (send empty string)
                lastTypingUpdateMs = currentTime;
                ClientPlayNetworking.send(new TypingIndicatorPayload(client.player.getName().getString(), true, ""));
            }
        }
    }
}
