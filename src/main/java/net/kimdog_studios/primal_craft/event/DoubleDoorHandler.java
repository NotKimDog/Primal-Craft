package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.client.config.PrimalCraftConfig;

/**
 * 🎮 Primal Craft - Double Doors Handler
 *
 * Synchronizes opening of multiple identical doors simultaneously.
 *
 * Features:
 * - Auto-detect adjacent identical doors
 * - Open/close in sync
 * - Works with all door types
 * - Fence gates & trapdoors support
 * - Configurable
 *
 * @author KimDog Studios
 * @version 1.0.0
 * @since 2026-01-28
 */
public final class DoubleDoorHandler {
    private DoubleDoorHandler() {}

    private static boolean lastDoubleDoorsState = false;

    public static void register() {
        PrimalCraft.LOGGER.info("🚪 [DOUBLE_DOORS] Registering Double Doors Handler");

        UseBlockCallback.EVENT.register(DoubleDoorHandler::onUseBlock);

        PrimalCraft.LOGGER.info("✅ [DOUBLE_DOORS] Double Doors Handler registered");
    }

    private static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            boolean doubleDoorsEnabled = isDoubleDoorsEnabled();
            if (doubleDoorsEnabled != lastDoubleDoorsState) {
                lastDoubleDoorsState = doubleDoorsEnabled;
                String status = doubleDoorsEnabled ? "ENABLED" : "DISABLED";
                PrimalCraft.LOGGER.debug("🚪 Double doors {}", status);
            }

            if (!doubleDoorsEnabled) {
                return ActionResult.PASS;
            }

            // Only work server-side
            if (world.getServer() == null) {
                return ActionResult.PASS;
            }

            BlockPos hitPos = hitResult.getBlockPos();
            BlockState hitBlockState = world.getBlockState(hitPos);
            Block hitBlock = hitBlockState.getBlock();

            // Check if it's a door, trapdoor, or fence gate
            if (!isDoorLike(hitBlock)) {
                return ActionResult.PASS;
            }

            // Find and sync adjacent identical doors
            syncAdjacentDoors(world, hitPos, hitBlockState, hitBlock);

            return ActionResult.PASS;

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DOUBLE_DOORS] Error syncing doors", e);
            return ActionResult.PASS;
        }
    }

    /**
     * Check if a block is a door-like object
     */
    private static boolean isDoorLike(Block block) {
        return block instanceof DoorBlock ||
               block instanceof TrapdoorBlock ||
               block instanceof FenceGateBlock;
    }

    /**
     * Sync adjacent identical doors
     */
    private static void syncAdjacentDoors(World world, BlockPos pos, BlockState blockState, Block block) {
        try {
            // Check all 4 horizontal directions
            Direction[] directions = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

            for (Direction direction : directions) {
                BlockPos adjacentPos = pos.offset(direction);
                BlockState adjacentState = world.getBlockState(adjacentPos);

                // Check if adjacent block is same type
                if (adjacentState.getBlock() == block) {
                    // Sync the door state
                    if (block instanceof DoorBlock) {
                        syncDoors(world, pos, adjacentPos, blockState, adjacentState);
                    } else if (block instanceof TrapdoorBlock) {
                        syncTrapdoors(world, pos, adjacentPos, blockState, adjacentState);
                    } else if (block instanceof FenceGateBlock) {
                        syncFenceGates(world, pos, adjacentPos, blockState, adjacentState);
                    }
                }
            }

            PrimalCraft.LOGGER.debug("🚪 [DOUBLE_DOORS] Synced doors at {}", pos);

        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DOUBLE_DOORS] Failed to sync doors", e);
        }
    }

    /**
     * Sync two doors
     */
    private static void syncDoors(World world, BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2) {
        try {
            boolean isOpen1 = state1.get(DoorBlock.OPEN);
            boolean isOpen2 = state2.get(DoorBlock.OPEN);

            if (isOpen1 != isOpen2) {
                // Sync to match
                BlockState newState2 = state2.with(DoorBlock.OPEN, isOpen1);
                world.setBlockState(pos2, newState2, 2);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DOUBLE_DOORS] Failed to sync door blocks", e);
        }
    }

    /**
     * Sync two trapdoors
     */
    private static void syncTrapdoors(World world, BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2) {
        try {
            boolean isOpen1 = state1.get(TrapdoorBlock.OPEN);
            boolean isOpen2 = state2.get(TrapdoorBlock.OPEN);

            if (isOpen1 != isOpen2) {
                BlockState newState2 = state2.with(TrapdoorBlock.OPEN, isOpen1);
                world.setBlockState(pos2, newState2, 2);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DOUBLE_DOORS] Failed to sync trapdoor blocks", e);
        }
    }

    /**
     * Sync two fence gates
     */
    private static void syncFenceGates(World world, BlockPos pos1, BlockPos pos2, BlockState state1, BlockState state2) {
        try {
            boolean isOpen1 = state1.get(FenceGateBlock.OPEN);
            boolean isOpen2 = state2.get(FenceGateBlock.OPEN);

            if (isOpen1 != isOpen2) {
                BlockState newState2 = state2.with(FenceGateBlock.OPEN, isOpen1);
                world.setBlockState(pos2, newState2, 2);
            }
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("[DOUBLE_DOORS] Failed to sync fence gate blocks", e);
        }
    }

    /**
     * Check if double doors is enabled
     */
    public static boolean isDoubleDoorsEnabled() {
        try {
            return PrimalCraftConfig.getAdvanced().integrations.webDashboardEnabled == false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Set double doors enabled state
     */
    public static void setDoubleDoorsEnabled(boolean enabled) {
        try {
            lastDoubleDoorsState = enabled;

            String status = enabled ? "enabled" : "disabled";
            PrimalCraft.LOGGER.info("🚪 [DOUBLE_DOORS] Double doors {}", status);
        } catch (Exception e) {
            PrimalCraft.LOGGER.error("[DOUBLE_DOORS] Failed to toggle double doors", e);
        }
    }
}
