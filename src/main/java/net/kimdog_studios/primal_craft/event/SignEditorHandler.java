package net.kimdog_studios.primal_craft.event;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kimdog_studios.primal_craft.network.OpenSignEditorPayload;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class SignEditorHandler {

    public static void register() {
        UseBlockCallback.EVENT.register(SignEditorHandler::onBlockUse);
    }

    private static ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        // Only handle right-click with main hand on server side
        if (hand != Hand.MAIN_HAND || !world.isClient()) {
            return ActionResult.PASS;
        }

        BlockState state = world.getBlockState(hitResult.getBlockPos());

        // Check if the block is a sign
        if (state.getBlock() instanceof SignBlock) {
            BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());

            if (blockEntity instanceof SignBlockEntity) {
                // Open custom sign editor when player right-clicks a sign
                if (player instanceof net.minecraft.server.network.ServerPlayerEntity serverPlayer) {
                    ServerPlayNetworking.send(serverPlayer, new OpenSignEditorPayload(hitResult.getBlockPos()));
                    return ActionResult.SUCCESS;
                }
            }
        }

        return ActionResult.PASS;
    }
}
