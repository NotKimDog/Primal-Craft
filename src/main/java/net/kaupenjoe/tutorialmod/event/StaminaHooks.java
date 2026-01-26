package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.network.SprintCooldownPayload;
import net.kaupenjoe.tutorialmod.util.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StaminaHooks {
    private static final double COST_SPRINT_TICK = 0.8;
    private static final double COST_JUMP = 8.0;
    private static final double COST_ATTACK = 5.0; // Punching/hitting (reduced slightly)
    private static final double COST_BLOCK_BREAK = 4.0; // Mining blocks (reduced slightly)
    private static final double COST_USE = 2.0; // Placing blocks / using items (reduced)
    private static final double COST_MOVEMENT = 0.5;
    private static final double COST_SWIM = 0.3;
    private static final double COST_CLIMB = 0.5;
    private static final double COST_CROUCH = 0.05;
    private static final double SPRINT_MIN_STAMINA = 20.0;
    private static final int SPRINT_COOLDOWN_TICKS = 100;

    private static final Map<UUID, Integer> lastSprintChargeTick = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> lastOnGround = new ConcurrentHashMap<>();
    private static final Map<UUID, Vec3d> lastPosition = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> sprintCooldownTick = new ConcurrentHashMap<>();

    private StaminaHooks() {}

    public static void register() {
        // Per-player tick via server tick
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            int tick = server.getTicks();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                World world = player.getEntityWorld();
                UUID id = player.getUuid();

                // Update cooldown
                int cooldownTick = sprintCooldownTick.getOrDefault(id, 0);
                if (cooldownTick > 0) {
                    sprintCooldownTick.put(id, cooldownTick - 1);
                    // Send cooldown to client
                    ServerPlayNetworking.send(player, new SprintCooldownPayload(cooldownTick - 1));
                } else if (cooldownTick == 0 && tick % 5 == 0) {
                    // Send 0 occasionally to keep HUD in sync
                    ServerPlayNetworking.send(player, new SprintCooldownPayload(0));
                }

                // Get weight penalty based on inventory
                double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(player);

                // Get all multipliers
                double potionMultiplier = StaminaPotionEffects.getDrainMultiplier(player);
                double tempMultiplier = TemperatureSystem.getTemperatureStaminaMultiplier(
                    TemperatureSystem.getPlayerTemperature(player)
                );
                double envMultiplier = 1.0; // EnvironmentalStaminaEffects.getBiomeDrainMultiplier(player);
                double armorMultiplier = 1.0; // ArmorWeightSystem.getArmorWeightMultiplier(player);

                // Get current position
                Vec3d currentPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                Vec3d lastPos = lastPosition.getOrDefault(id, currentPos);
                double distanceMoved = currentPos.squaredDistanceTo(lastPos);
                lastPosition.put(id, currentPos);

                // Sprint drain every tick while sprinting (increased by weight)
                if (player.isSprinting()) {
                    // Enforce minimum stamina requirement and cooldown
                    double currentStamina = StaminaSystem.get(player);
                    if (currentStamina < SPRINT_MIN_STAMINA) {
                        player.setSprinting(false);
                    } else if (sprintCooldownTick.getOrDefault(id, 0) > 0) {
                        player.setSprinting(false); // Can't sprint during cooldown
                    } else if (distanceMoved > 0.0001) { // Only drain if actually moving
                        double sprintCost = COST_SPRINT_TICK * (1.0 + weightPenalty) * potionMultiplier * tempMultiplier * envMultiplier * armorMultiplier;
                        if (!StaminaSystem.tryConsume(player, sprintCost)) {
                            player.setSprinting(false);
                            sprintCooldownTick.put(id, SPRINT_COOLDOWN_TICKS); // Start cooldown
                        }
                    }
                } else {
                    // Walking movement drain (only if moving and not sprinting)
                    if (distanceMoved > 0.0001 && !player.isSwimming() && !player.isClimbing()) {
                        double movementCost = (COST_MOVEMENT + weightPenalty * 0.5);
                        StaminaSystem.tryConsume(player, movementCost);
                    }
                }

                // Swimming drain (affected by weight)
                if (player.isSwimming()) {
                    double swimCost = COST_SWIM * (1.0 + weightPenalty * 0.5);
                    StaminaSystem.tryConsume(player, swimCost);
                }

                // Climbing drain (ladder/vine, affected by weight)
                if (player.isClimbing()) {
                    double climbCost = COST_CLIMB * (1.0 + weightPenalty);
                    StaminaSystem.tryConsume(player, climbCost);
                }

                // Crouching drain (sneaking, affected by weight)
                if (player.isInSneakingPose()) {
                    double crouchCost = COST_CROUCH * (1.0 + weightPenalty * 0.3);
                    StaminaSystem.tryConsume(player, crouchCost);
                }

                // Jump detection (affected by weight)
                boolean onGround = player.isOnGround();
                boolean wasOnGround = lastOnGround.getOrDefault(id, true);
                if (wasOnGround && !onGround) {
                    double jumpCost = COST_JUMP * (1.0 + weightPenalty);
                    if (!StaminaSystem.tryConsume(player, jumpCost)) {
                        player.setVelocity(player.getVelocity().x, Math.min(0, player.getVelocity().y), player.getVelocity().z);
                        player.velocityDirty = true;
                    }
                }
                lastOnGround.put(id, onGround);
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
            double attackCost = COST_ATTACK * (1.0 + weightPenalty * 0.5);
            if (!StaminaSystem.tryConsume(serverPlayer, attackCost)) {
                return ActionResult.FAIL; // Prevent attack if no stamina
            }
            return ActionResult.PASS;
        });

        // Block breaking (mining) - this is called when you start breaking a block
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
            double breakCost = COST_BLOCK_BREAK * (1.0 + weightPenalty * 0.3);
            if (!StaminaSystem.tryConsume(serverPlayer, breakCost)) {
                return ActionResult.FAIL; // Prevent block break if no stamina
            }
            return ActionResult.PASS;
        });

        // Block/item use (placing blocks, eating, drawing bows, etc)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClient() || !(player instanceof ServerPlayerEntity serverPlayer)) return ActionResult.PASS;
            double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
            double useCost = COST_USE * (1.0 + weightPenalty * 0.2);
            if (!StaminaSystem.tryConsume(serverPlayer, useCost)) {
                return ActionResult.FAIL; // Prevent block use if no stamina
            }
            return ActionResult.PASS;
        });
    }
}
