package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Plays sound effects based on stamina level (client-side)
 */
public final class StaminaSoundEffects {
    private static int breathingTick = 0;
    private static int heartbeatTick = 0;
    private static double lastStamina = 100.0;
    private static boolean hasPlayedLowEnergySound = false;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // Get stamina from HUD overlay
            double stamina = StaminaHudOverlay.getClientStamina();
            double maxStamina = StaminaHudOverlay.getClientMaxStamina();
            float fill = (float) (stamina / maxStamina);

            // Play ENERGY_LOW sound when stamina drops below 30% (once per depletion)
            if (fill < 0.3f && !hasPlayedLowEnergySound) {
                client.player.playSound(net.kimdog_studios.primal_craft.sound.ModSounds.ENERGY_LOW, 0.7f, 1.0f);
                hasPlayedLowEnergySound = true;
            } else if (fill >= 0.5f) {
                // Reset flag when stamina recovers to 50% or above
                hasPlayedLowEnergySound = false;
            }

            // Heavy breathing when low on stamina (below 40%)
            if (fill < 0.4f) {
                breathingTick++;
                int breathInterval = (int) (60 - (fill * 100)); // Faster breathing when lower

                if (breathingTick > breathInterval) {
                    breathingTick = 0;
                }
            } else {
                breathingTick = 0;
            }

            // Heartbeat when critical (below 20%)
            if (fill < 0.2f) {
                heartbeatTick++;
                int heartInterval = 20 - (int) (fill * 40); // Faster heartbeat when lower

                if (heartbeatTick > heartInterval) {
                    client.world.playSound(
                        client.player,
                        client.player.getBlockPos(),
                        SoundEvents.ENTITY_WARDEN_HEARTBEAT,
                        SoundCategory.PLAYERS,
                        0.4f,
                        1.0f + (0.2f - fill) // Higher pitch when more critical
                    );
                    heartbeatTick = 0;
                }
            } else {
                heartbeatTick = 0;
            }

            // Exhausted gasp when stamina runs out
            if (stamina < 5.0 && lastStamina >= 5.0) {
                client.world.playSound(
                    client.player,
                    client.player.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_HURT,
                    SoundCategory.PLAYERS,
                    0.5f,
                    0.6f
                );
            }

            lastStamina = stamina;
        });
    }

    // Helper methods for HUD to expose stamina values
    private static class StaminaHudOverlay {
        private static double clientStamina = 100.0;
        private static double clientMaxStamina = 100.0;

        public static double getClientStamina() {
            // This will be linked to actual HUD overlay
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                // Estimate from player's hunger/health for now
                return Math.min(100, client.player.getHealth() * 5);
            }
            return clientStamina;
        }

        public static double getClientMaxStamina() {
            return clientMaxStamina;
        }

        public static void updateValues(double stamina, double maxStamina) {
            clientStamina = stamina;
            clientMaxStamina = maxStamina;
        }
    }
}
