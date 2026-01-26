package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.LoginStreakPayload;
import net.kaupenjoe.tutorialmod.util.LoginStreakService;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

/**
 * Updates and syncs the daily login streak when players join.
 */
public final class LoginStreakHandler {
    private LoginStreakHandler() {}

    // Milestone sets: 7, 30, 100, then every 50 after 100
    private static boolean isMilestone(int streak) {
        if (streak == 7 || streak == 30 || streak == 100) return true;
        if (streak > 100 && (streak - 100) % 50 == 0) return true;
        return false;
    }

    private static boolean isBigMilestone(int streak) {
        return streak == 100 || streak == 365;
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            var result = LoginStreakService.updateAndGetWithEvent(player, server);

            // Notify client HUD
            ServerPlayNetworking.send(player, new LoginStreakPayload(result.streak(), result.lastDay(), result.increased(), result.broken(), result.previous()));

            // Sound cues with pitch scaling
            if (result.increased()) {
                float pitch = 1.25f + Math.min(0.5f, result.streak() / 500f); // gradually increase pitch up to 1.75
                player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 0.8f, pitch);
            }
            if (result.broken()) {
                player.playSound(SoundEvents.ENTITY_PLAYER_BREATH, 0.6f, 0.9f);
            }

            // Particles on increase
            if (result.increased()) {
                if (player.getEntityWorld() instanceof ServerWorld sw) {
                    int count = isBigMilestone(result.streak()) ? 36 : (isMilestone(result.streak()) ? 20 : 12);
                    for (int i = 0; i < count; i++) {
                        double ox = (player.getRandom().nextDouble() - 0.5) * 0.6;
                        double oy = player.getRandom().nextDouble() * 0.8 + 0.2;
                        double oz = (player.getRandom().nextDouble() - 0.5) * 0.6;
                        sw.spawnParticles(ParticleTypes.FLAME, player.getX() + ox, player.getY() + 1.2 + oy, player.getZ() + oz, 1, 0.0, 0.0, 0.0, 0.01);
                    }
                    // Confetti on big milestones
                    if (isBigMilestone(result.streak())) {
                        for (int i = 0; i < 24; i++) {
                            double ox = (player.getRandom().nextDouble() - 0.5) * 1.2;
                            double oy = player.getRandom().nextDouble() * 1.5 + 0.5;
                            double oz = (player.getRandom().nextDouble() - 0.5) * 1.2;
                            sw.spawnParticles(ParticleTypes.END_ROD, player.getX() + ox, player.getY() + 1.5 + oy, player.getZ() + oz, 1, 0.0, 0.0, 0.0, 0.02);
                        }
                    }
                }
            }

            // Broken streak notice
            if (result.broken()) {
                var msg = net.minecraft.text.Text.literal("🔥 Streak broken at "+result.previous()+"! Starting over.").withColor(0xFFAA66);
                player.sendMessage(msg, false);
            }
        });
    }
}
