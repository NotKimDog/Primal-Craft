package net.kimdog_studios.primal_craft.item.custom;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class HammerItem extends Item {
    public HammerItem(ToolMaterial material, float attackDamage, float attackSpeed, Settings settings) {
        super(settings.pickaxe(material, attackDamage, attackSpeed));
    }

    public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initialPos, World world, ServerPlayerEntity player) {
        // Always mine a 3x3 area; ignore incoming range
        int clampedRange = 1;

        HitResult hit = player.raycast(20, 0, false);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return List.of(initialPos);
        }

        BlockHitResult blockHit = (BlockHitResult) hit;
        Direction side = blockHit.getSide();

        // Use a LinkedHashSet to avoid duplicates while keeping a stable order (center first)
        Set<BlockPos> positions = new LinkedHashSet<>();
        positions.add(initialPos);

        // Use provided world (server side caller)
        java.util.function.Consumer<BlockPos> tryAdd = pos -> {
            BlockState state = world.getBlockState(pos);
            // Only skip blocks with negative hardness (unbreakable); include everything else so mixed blocks still get mined
            if (state.getHardness(world, pos) < 0) return;
            positions.add(pos);
        };

        // Build the hammer pattern based on the side hit
        if (side == Direction.DOWN || side == Direction.UP) {
            for (int dx = -clampedRange; dx <= clampedRange; dx++) {
                for (int dz = -clampedRange; dz <= clampedRange; dz++) {
                    tryAdd.accept(initialPos.add(dx, 0, dz));
                }
            }
        } else if (side == Direction.NORTH || side == Direction.SOUTH) {
            for (int dx = -clampedRange; dx <= clampedRange; dx++) {
                for (int dy = -clampedRange; dy <= clampedRange; dy++) {
                    tryAdd.accept(initialPos.add(dx, dy, 0));
                }
            }
        } else if (side == Direction.EAST || side == Direction.WEST) {
            for (int dz = -clampedRange; dz <= clampedRange; dz++) {
                for (int dy = -clampedRange; dy <= clampedRange; dy++) {
                    tryAdd.accept(initialPos.add(0, dy, dz));
                }
            }
        }

        return new ArrayList<>(positions);
    }
}
