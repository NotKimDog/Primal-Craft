package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.TutorialMod;
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
    private static final double COST_ATTACK = 5.0;
    private static final double COST_BLOCK_BREAK = 4.0;
    private static final double COST_USE = 2.0;
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

    private static int ticksProcessed = 0;
    private static int sprintsTriggered = 0;
    private static int jumpsTriggered = 0;
    private static int attacksTriggered = 0;
    private static int blockBreaksTriggered = 0;
    private static int blockUsesTriggered = 0;

    private StaminaHooks() {}

    public static void register() {
        TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        TutorialMod.LOGGER.info("║  REGISTERING STAMINA HOOKS EVENT SYSTEM                   ║");
        TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        try {
            // Per-player tick via server tick
            ServerTickEvents.END_SERVER_TICK.register(server -> {
                int tick = server.getTicks();
                int playerCount = server.getPlayerManager().getPlayerList().size();

                TutorialMod.LOGGER.trace("📍 [SERVER_TICK] Tick #{} - Processing {} players", tick, playerCount);

                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    World world = player.getEntityWorld();
                    UUID id = player.getUuid();
                    ticksProcessed++;

                    // Update cooldown
                    int cooldownTick = sprintCooldownTick.getOrDefault(id, 0);
                    if (cooldownTick > 0) {
                        sprintCooldownTick.put(id, cooldownTick - 1);
                        TutorialMod.LOGGER.trace("   ├─ [COOLDOWN] {} cooldown: {} -> {}",
                            player.getName().getString(), cooldownTick, cooldownTick - 1);
                        ServerPlayNetworking.send(player, new SprintCooldownPayload(cooldownTick - 1));
                    } else if (cooldownTick == 0 && tick % 5 == 0) {
                        TutorialMod.LOGGER.trace("   ├─ [SYNC] Syncing sprint cooldown to client: 0");
                        ServerPlayNetworking.send(player, new SprintCooldownPayload(0));
                    }

                    // Get weight penalty based on inventory
                    double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(player);
                    TutorialMod.LOGGER.trace("   ├─ [WEIGHT] {} weight penalty: {}",
                        player.getName().getString(), String.format("%.2f%%", weightPenalty * 100));

                    // Get all multipliers
                    double potionMultiplier = StaminaPotionEffects.getDrainMultiplier(player);
                    double currentTemp = TemperatureSystem.getPlayerTemperature(player);
                    double tempMultiplier = TemperatureSystem.getTemperatureStaminaMultiplier(currentTemp);
                    double envMultiplier = 1.0;
                    double armorMultiplier = 1.0;

                    TutorialMod.LOGGER.trace("   ├─ [MULTIPLIERS] Potion: {}, Temp: {} ({}°C), Env: {}, Armor: {}",
                        String.format("%.2f", potionMultiplier), String.format("%.2f", tempMultiplier),
                        String.format("%.1f", currentTemp), String.format("%.2f", envMultiplier),
                        String.format("%.2f", armorMultiplier));

                    // Get current position
                    Vec3d currentPos = new Vec3d(player.getX(), player.getY(), player.getZ());
                    Vec3d lastPos = lastPosition.getOrDefault(id, currentPos);
                    double distanceMoved = currentPos.squaredDistanceTo(lastPos);
                    lastPosition.put(id, currentPos);

                    TutorialMod.LOGGER.trace("   ├─ [POSITION] Moved: {} blocks", String.format("%.3f", Math.sqrt(distanceMoved)));

                    // Sprint drain every tick while sprinting
                    if (player.isSprinting()) {
                        double currentStamina = StaminaSystem.get(player);
                        TutorialMod.LOGGER.trace("   ├─ [SPRINT] Current stamina: {}", String.format("%.1f", currentStamina));

                        if (currentStamina < SPRINT_MIN_STAMINA) {
                            TutorialMod.LOGGER.debug("   │  ⚠️  [SPRINT] Below minimum stamina ({}), stopping sprint",
                                String.format("%.1f", SPRINT_MIN_STAMINA));
                            player.setSprinting(false);
                        } else if (sprintCooldownTick.getOrDefault(id, 0) > 0) {
                            TutorialMod.LOGGER.trace("   │  ⚠️  [SPRINT] In cooldown, stopping sprint");
                            player.setSprinting(false);
                        } else if (distanceMoved > 0.0001) {
                            double sprintCost = COST_SPRINT_TICK * (1.0 + weightPenalty) * potionMultiplier * tempMultiplier * envMultiplier * armorMultiplier;
                            TutorialMod.LOGGER.trace("   │  ⚡ [SPRINT] Cost calculation: {} * (1.0 + {}) * {} * {} = {}",
                                COST_SPRINT_TICK, String.format("%.2f", weightPenalty),
                                String.format("%.2f", potionMultiplier), String.format("%.2f", tempMultiplier),
                                String.format("%.2f", sprintCost));

                            if (!StaminaSystem.tryConsume(player, sprintCost)) {
                                sprintsTriggered++;
                                TutorialMod.LOGGER.debug("   │  ✗ [SPRINT] Failed to consume {}, starting cooldown (Event #{})",
                                    String.format("%.2f", sprintCost), sprintsTriggered);
                                player.setSprinting(false);
                                sprintCooldownTick.put(id, SPRINT_COOLDOWN_TICKS);
                            } else {
                                TutorialMod.LOGGER.trace("   │  ✓ [SPRINT] Consumed {} stamina", String.format("%.2f", sprintCost));
                            }
                        }
                    } else {
                        // Walking movement drain
                        if (distanceMoved > 0.0001 && !player.isSwimming() && !player.isClimbing()) {
                            double movementCost = (COST_MOVEMENT + weightPenalty * 0.5);
                            TutorialMod.LOGGER.trace("   ├─ [MOVEMENT] Cost: {}", String.format("%.2f", movementCost));
                            StaminaSystem.tryConsume(player, movementCost);
                        }
                    }

                    // Swimming drain
                    if (player.isSwimming()) {
                        double swimCost = COST_SWIM * (1.0 + weightPenalty * 0.5);
                        TutorialMod.LOGGER.trace("   ├─ [SWIM] Cost: {}", String.format("%.2f", swimCost));
                        StaminaSystem.tryConsume(player, swimCost);
                    }

                    // Climbing drain
                    if (player.isClimbing()) {
                        double climbCost = COST_CLIMB * (1.0 + weightPenalty);
                        TutorialMod.LOGGER.trace("   ├─ [CLIMB] Cost: {}", String.format("%.2f", climbCost));
                        StaminaSystem.tryConsume(player, climbCost);
                    }

                    // Crouching drain
                    if (player.isInSneakingPose()) {
                        double crouchCost = COST_CROUCH * (1.0 + weightPenalty * 0.3);
                        TutorialMod.LOGGER.trace("   ├─ [CROUCH] Cost: {}", String.format("%.2f", crouchCost));
                        StaminaSystem.tryConsume(player, crouchCost);
                    }

                    // Jump detection
                    boolean onGround = player.isOnGround();
                    boolean wasOnGround = lastOnGround.getOrDefault(id, true);
                    if (wasOnGround && !onGround) {
                        jumpsTriggered++;
                        double jumpCost = COST_JUMP * (1.0 + weightPenalty);
                        TutorialMod.LOGGER.debug("📍 [JUMP] Event #{} for {}", jumpsTriggered, player.getName().getString());
                        TutorialMod.LOGGER.trace("   ├─ Cost: {}", String.format("%.2f", jumpCost));

                        if (!StaminaSystem.tryConsume(player, jumpCost)) {
                            TutorialMod.LOGGER.debug("   ├─ ✗ Insufficient stamina, canceling jump");
                            player.setVelocity(player.getVelocity().x, Math.min(0, player.getVelocity().y), player.getVelocity().z);
                            player.velocityDirty = true;
                        } else {
                            TutorialMod.LOGGER.trace("   └─ ✓ Jump approved");
                        }
                    }
                    lastOnGround.put(id, onGround);
                }

                if (tick % 100 == 0) {
                    TutorialMod.LOGGER.debug("📊 [TICK STATS] Processed {} ticks with {} sprints, {} jumps, {} attacks, {} breaks, {} uses",
                        ticksProcessed, sprintsTriggered, jumpsTriggered, attacksTriggered, blockBreaksTriggered, blockUsesTriggered);
                }
            });

            TutorialMod.LOGGER.debug("✓ Server tick event registered");

            AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
                attacksTriggered++;

                if (world.isClient()) {
                    TutorialMod.LOGGER.trace("📍 [ATTACK] Client-side, skipping");
                    return ActionResult.PASS;
                }

                if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                    TutorialMod.LOGGER.trace("📍 [ATTACK] Not a ServerPlayerEntity, skipping");
                    return ActionResult.PASS;
                }

                TutorialMod.LOGGER.debug("📍 [ATTACK] Event #{} - {} attacking {}",
                    attacksTriggered, serverPlayer.getName().getString(), entity.getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Target: {}", entity.getClass().getSimpleName());
                TutorialMod.LOGGER.trace("   ├─ Hand: {}", hand.name());

                double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
                double attackCost = COST_ATTACK * (1.0 + weightPenalty * 0.5);

                TutorialMod.LOGGER.trace("   ├─ Weight penalty: {}", String.format("%.2f%%", weightPenalty * 100));
                TutorialMod.LOGGER.trace("   ├─ Attack cost: {}", String.format("%.2f", attackCost));
                TutorialMod.LOGGER.trace("   ├─ Current stamina: {}", String.format("%.1f", StaminaSystem.get(serverPlayer)));

                if (!StaminaSystem.tryConsume(serverPlayer, attackCost)) {
                    TutorialMod.LOGGER.debug("   └─ ✗ Insufficient stamina, attack blocked");
                    return ActionResult.FAIL;
                }

                TutorialMod.LOGGER.trace("   └─ ✓ Attack approved");
                return ActionResult.PASS;
            });

            TutorialMod.LOGGER.debug("✓ AttackEntity event registered");

            AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
                blockBreaksTriggered++;

                if (world.isClient()) {
                    TutorialMod.LOGGER.trace("📍 [BREAK] Client-side, skipping");
                    return ActionResult.PASS;
                }

                if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                    TutorialMod.LOGGER.trace("📍 [BREAK] Not a ServerPlayerEntity, skipping");
                    return ActionResult.PASS;
                }

                TutorialMod.LOGGER.debug("📍 [BREAK] Event #{} - {} breaking block",
                    blockBreaksTriggered, serverPlayer.getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Position: X={}, Y={}, Z={}", pos.getX(), pos.getY(), pos.getZ());
                TutorialMod.LOGGER.trace("   ├─ Direction: {}", direction.name());
                TutorialMod.LOGGER.trace("   ├─ Block: {}", world.getBlockState(pos).getBlock().getName().getString());

                double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
                double breakCost = COST_BLOCK_BREAK * (1.0 + weightPenalty * 0.3);

                TutorialMod.LOGGER.trace("   ├─ Weight penalty: {}", String.format("%.2f%%", weightPenalty * 100));
                TutorialMod.LOGGER.trace("   ├─ Break cost: {}", String.format("%.2f", breakCost));
                TutorialMod.LOGGER.trace("   ├─ Current stamina: {}", String.format("%.1f", StaminaSystem.get(serverPlayer)));

                if (!StaminaSystem.tryConsume(serverPlayer, breakCost)) {
                    TutorialMod.LOGGER.debug("   └─ ✗ Insufficient stamina, block break blocked");
                    return ActionResult.FAIL;
                }

                TutorialMod.LOGGER.trace("   └─ ✓ Block break approved");
                return ActionResult.PASS;
            });

            TutorialMod.LOGGER.debug("✓ AttackBlock event registered");

            UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
                blockUsesTriggered++;

                if (world.isClient()) {
                    TutorialMod.LOGGER.trace("📍 [USE] Client-side, skipping");
                    return ActionResult.PASS;
                }

                if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                    TutorialMod.LOGGER.trace("📍 [USE] Not a ServerPlayerEntity, skipping");
                    return ActionResult.PASS;
                }

                BlockPos pos = hitResult.getBlockPos();
                TutorialMod.LOGGER.debug("📍 [USE] Event #{} - {} using block",
                    blockUsesTriggered, serverPlayer.getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Position: X={}, Y={}, Z={}", pos.getX(), pos.getY(), pos.getZ());
                TutorialMod.LOGGER.trace("   ├─ Block: {}", world.getBlockState(pos).getBlock().getName().getString());
                TutorialMod.LOGGER.trace("   ├─ Hand: {}", hand.name());

                double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
                double useCost = COST_USE * (1.0 + weightPenalty * 0.2);

                TutorialMod.LOGGER.trace("   ├─ Weight penalty: {}", String.format("%.2f%%", weightPenalty * 100));
                TutorialMod.LOGGER.trace("   ├─ Use cost: {}", String.format("%.2f", useCost));
                TutorialMod.LOGGER.trace("   ├─ Current stamina: {}", String.format("%.1f", StaminaSystem.get(serverPlayer)));

                if (!StaminaSystem.tryConsume(serverPlayer, useCost)) {
                    TutorialMod.LOGGER.debug("   └─ ✗ Insufficient stamina, block use blocked");
                    return ActionResult.FAIL;
                }

                TutorialMod.LOGGER.trace("   └─ ✓ Block use approved");
                return ActionResult.PASS;
            });

            TutorialMod.LOGGER.debug("✓ UseBlock event registered");

            TutorialMod.LOGGER.info("✅ [STAMINA HOOKS] All event handlers registered successfully");
        } catch (Exception e) {
            TutorialMod.LOGGER.error("❌ [STAMINA HOOKS] Failed to register event handlers", e);
        }
    }
}
