package net.kaupenjoe.tutorialmod.event;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.network.ChatAnimatedPayload;
import net.kaupenjoe.tutorialmod.util.ItemWeightSystem;
import net.kaupenjoe.tutorialmod.util.StaminaSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class VeinMinerHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("KimDog VeinMiner");
    private static final java.util.Set<Block> ORE_BLOCKS = new java.util.HashSet<>();
    private static final int MAX_BLOCKS = 512;
    private static final int MAX_RANGE = 64;
    private static final int BREAK_DELAY_MS = 50;
    private static final int BREAK_DELAY_TICKS = 2;
    private static final boolean ENABLE_CASCADE = true;
    private static final boolean ENABLE_PARTICLES = true;
    private static final boolean ENABLE_SOUNDS = true;
    private static final boolean CONSOLIDATE_DROPS = true;
    private static final boolean ENABLE_PARTICLE_TRAILS = true;
    private static final boolean ENABLE_BREAK_INDICATORS = true;
    private static final String PARTICLE_EFFECT = "enchant"; // rainbow, ore, enchant, smoke
    private static final int PARTICLE_COUNT = 10;

    private static String getRandomParticleEffect() {
        String[] effects = {"rainbow", "ore", "enchant", "smoke"};
        return effects[(int) (Math.random() * effects.length)];
    }

    static {
        // Initialize ore blocks
        ORE_BLOCKS.add(Blocks.COAL_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_COAL_ORE);
        ORE_BLOCKS.add(Blocks.IRON_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_IRON_ORE);
        ORE_BLOCKS.add(Blocks.COPPER_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_COPPER_ORE);
        ORE_BLOCKS.add(Blocks.GOLD_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_GOLD_ORE);
        ORE_BLOCKS.add(Blocks.DIAMOND_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_DIAMOND_ORE);
        ORE_BLOCKS.add(Blocks.EMERALD_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_EMERALD_ORE);
        ORE_BLOCKS.add(Blocks.LAPIS_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_LAPIS_ORE);
        ORE_BLOCKS.add(Blocks.REDSTONE_ORE);
        ORE_BLOCKS.add(Blocks.DEEPSLATE_REDSTONE_ORE);
        ORE_BLOCKS.add(Blocks.NETHER_GOLD_ORE);
        ORE_BLOCKS.add(Blocks.NETHER_QUARTZ_ORE);
        ORE_BLOCKS.add(Blocks.ANCIENT_DEBRIS);
        ORE_BLOCKS.add(ModBlocks.PINK_GARNET_ORE);
        ORE_BLOCKS.add(ModBlocks.PINK_GARNET_DEEPSLATE_ORE);
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (!(world instanceof ServerWorld)) return;
            if (!(player instanceof ServerPlayerEntity)) return;

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            ServerWorld serverWorld = (ServerWorld) world;

            // Check if block is an ore
            if (!isOreBlock(state.getBlock())) {
                return;
            }

            // Find all adjacent ore blocks of the same type (BFS)
            Predicate<BlockState> matchPredicate = createMatchPredicate(state);
            List<BlockPos> veinBlocks = searchVein(serverWorld, pos, state, MAX_BLOCKS - 1, MAX_RANGE, matchPredicate);

            if (veinBlocks.isEmpty()) {
                return; // No adjacent ores found
            }

            // Check if player has enough stamina for veinmining
            double weightPenalty = ItemWeightSystem.calculateInventoryWeightPenalty(serverPlayer);
            double totalVeinCost = veinBlocks.size() * 0.5 * (1.0 + weightPenalty * 0.3);

            if (!StaminaSystem.tryConsume(serverPlayer, totalVeinCost)) {
                return; // Not enough stamina to veinmine
            }

            LOGGER.info("VeinMiner: Breaking {} blocks for {}", veinBlocks.size(), serverPlayer.getName().getString());

            // Spawn activation animation
            spawnActivationAnimation(serverWorld, pos);

            // Apply breaks with cascade effect
            applyBreaks(serverPlayer, serverWorld, veinBlocks, pos, serverPlayer.getMainHandStack());

            // Spawn completion animation
            spawnCompletionAnimation(serverWorld, pos, veinBlocks.size());
        });
    }

    private static void applyBreaks(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, BlockPos originPos, ItemStack tool) {
        if (ENABLE_CASCADE && BREAK_DELAY_MS > 0) {
            applyBreaksWithCascade(player, world, positions, originPos, tool);
        } else {
            applyBreaksInstantly(player, world, positions, originPos, tool);
        }
    }

    private static void applyBreaksWithCascade(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, BlockPos originPos, ItemStack tool) {
        LOGGER.info(" Breaking {} blocks with cascade effect...", positions.size());

        sendVeinChat(player, "Mining " + positions.size() + " blocks!");

        // Use a thread-safe list for collecting drops
        List<ItemEntity> allDrops = java.util.Collections.synchronizedList(new ArrayList<>());
        int delayTicks = BREAK_DELAY_TICKS;

        // Schedule each block break with increasing delay
        for (int i = 0; i < positions.size(); i++) {
            final int index = i;
            final BlockPos blockPos = positions.get(i);
            final int scheduledTick = delayTicks * (index + 1);

            // Schedule the block break
            world.getServer().execute(() -> {
                java.util.Timer timer = new java.util.Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        world.getServer().execute(() -> {
                            BlockState state = world.getBlockState(blockPos);
                            if (state.isAir()) return;

                            LOGGER.info(" Breaking block {} at {}, {}, {}", index + 1, blockPos.getX(), blockPos.getY(), blockPos.getZ());

                            // Particle trail from origin to current block
                            if (ENABLE_PARTICLE_TRAILS) {
                                spawnParticleTrail(world, originPos, blockPos, index, positions.size());
                            }

                            // Particle effects
                            if (ENABLE_PARTICLES) {
                                spawnParticles(world, blockPos, state);
                            }

                            // Break indicator
                            if (ENABLE_BREAK_INDICATORS) {
                                spawnBlockBreakIndicator(world, blockPos, index, positions.size());
                            }

                            // Sound effects
                            if (ENABLE_SOUNDS) {
                                playSoundEffect(world, blockPos);
                            }

                            // Get drops with proper enchantment handling
                            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(blockPos);

                            // Get drops respecting all enchantments (Fortune, Silk Touch, etc.)
                            List<ItemStack> drops = Block.getDroppedStacks(state, world, blockPos, be, player, tool);

                            if (CONSOLIDATE_DROPS) {
                                // Add all drops to the synchronized list
                                for (ItemStack stack : drops) {
                                    // Create a copy of the stack to avoid reference issues
                                    ItemStack stackCopy = stack.copy();
                                    ItemEntity itemEntity = new ItemEntity(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, stackCopy);
                                    allDrops.add(itemEntity);
                                }
                            } else {
                                // Drop items immediately
                                Block.dropStacks(state, world, blockPos, be, player, tool);
                            }

                            // Break the block
                            world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 3);
                            tool.damage(1, player, Hand.MAIN_HAND);

                            // Spawn consolidated drops only on the last block
                            if (index == positions.size() - 1 && CONSOLIDATE_DROPS && !allDrops.isEmpty()) {
                                BlockPos dropPos = originPos;
                                for (ItemEntity itemEntity : allDrops) {
                                    itemEntity.setPosition(dropPos.getX() + 0.5, dropPos.getY() + 1.0, dropPos.getZ() + 0.5);
                                    world.spawnEntity(itemEntity);
                                }
                                allDrops.clear(); // Clear to avoid double-spawning
                            }
                        });
                    }
                }, scheduledTick * 50L);
            });
        }
    }

    private static void applyBreaksInstantly(ServerPlayerEntity player, ServerWorld world, List<BlockPos> positions, BlockPos originPos, ItemStack tool) {
        LOGGER.info(" Breaking {} blocks instantly...", positions.size());

        sendVeinChat(player, "Mining " + positions.size() + " blocks!");

        List<ItemEntity> allDrops = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++) {
            BlockPos p = positions.get(i);
            BlockState state = world.getBlockState(p);
            if (state.isAir()) continue;

            // Spawn particles
            if (ENABLE_PARTICLES) {
                spawnParticles(world, p, state);
            }

            // Play sound
            if (ENABLE_SOUNDS) {
                world.playSound(null, p, state.getSoundGroup().getBreakSound(), net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
            }

            // Get drops with proper enchantment handling
            net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(p);
            List<ItemStack> drops = Block.getDroppedStacks(state, world, p, be, player, tool);

            // Collect drops
            if (CONSOLIDATE_DROPS) {
                for (ItemStack stack : drops) {
                    // Create a copy of the stack to avoid reference issues
                    ItemStack stackCopy = stack.copy();
                    ItemEntity itemEntity = new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, stackCopy);
                    allDrops.add(itemEntity);
                }
            } else {
                Block.dropStacks(state, world, p, be, player, tool);
            }

            // Break the block
            world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
            tool.damage(1, player, Hand.MAIN_HAND);
        }

        // Spawn consolidated drops at origin
        if (CONSOLIDATE_DROPS && !allDrops.isEmpty()) {
            BlockPos dropPos = originPos;
            for (ItemEntity itemEntity : allDrops) {
                itemEntity.setPosition(dropPos.getX() + 0.5, dropPos.getY() + 1.0, dropPos.getZ() + 0.5);
                world.spawnEntity(itemEntity);
            }
        }
    }

    private static void spawnParticles(ServerWorld world, BlockPos pos, BlockState state) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        try {
            String effect = getRandomParticleEffect();
            switch (effect.toLowerCase()) {
                case "rainbow":
                    // Rainbow enchant particles
                    for (int i = 0; i < PARTICLE_COUNT / 2; i++) {
                        world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0.3, 0.3, 0.3, 0.15);
                    }
                    break;
                case "ore":
                    // Block-specific particles
                    BlockStateParticleEffect effect_obj = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
                    world.spawnParticles(effect_obj, x, y, z, PARTICLE_COUNT, 0.5, 0.5, 0.5, 0.15);
                    break;
                case "enchant":
                    // Enchant particles with spread
                    world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, PARTICLE_COUNT / 2, 0.4, 0.4, 0.4, 0.2);
                    break;
                case "smoke":
                default:
                    // Smoke particles
                    world.spawnParticles(ParticleTypes.SMOKE, x, y, z, PARTICLE_COUNT / 2, 0.3, 0.3, 0.3, 0.1);
                    break;
            }
        } catch (Exception e) {
            LOGGER.debug("Particle effect error: {}", e.getMessage());
        }
    }

    private static List<BlockPos> searchVein(World world, BlockPos start, BlockState originState, int maxBlocks, int maxRange, Predicate<BlockState> matchPredicate) {
        List<BlockPos> results = new ArrayList<>();
        java.util.Queue<BlockPos> queue = new java.util.ArrayDeque<>();
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();

        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty() && results.size() < maxBlocks) {
            BlockPos pos = queue.poll();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;

                        BlockPos offsetPos = pos.add(dx, dy, dz);

                        // Skip if already visited
                        if (visited.contains(offsetPos)) continue;

                        // Skip if out of range
                        if (Math.abs(offsetPos.getX() - start.getX()) > maxRange ||
                            Math.abs(offsetPos.getY() - start.getY()) > maxRange ||
                            Math.abs(offsetPos.getZ() - start.getZ()) > maxRange) {
                            visited.add(offsetPos);
                            continue;
                        }

                        BlockState state = world.getBlockState(offsetPos);
                        visited.add(offsetPos);

                        // If it matches, add to results and queue for further searching
                        if (matchPredicate.test(state)) {
                            results.add(offsetPos);
                            queue.add(offsetPos);
                        }
                    }
                }
            }
        }

        return results;
    }

    private static Predicate<BlockState> createMatchPredicate(BlockState originState) {
        Block originBlock = originState.getBlock();
        String originBlockName = Registries.BLOCK.getId(originBlock).getPath();
        String baseOreType = getBaseOreType(originBlockName);

        return (blockState) -> {
            Block block = blockState.getBlock();
            if (block == originBlock) return true;
            String blockName = Registries.BLOCK.getId(block).getPath();
            String blockBaseType = getBaseOreType(blockName);
            return baseOreType.equals(blockBaseType) && isOreBlock(block);
        };
    }

    private static String getBaseOreType(String blockName) {
        if (blockName.startsWith("deepslate_")) {
            return blockName.substring("deepslate_".length());
        }
        return blockName;
    }

    private static boolean isOreBlock(Block block) {
        return ORE_BLOCKS.contains(block);
    }


    public static void addOreBlock(Block block) {
        ORE_BLOCKS.add(block);
    }

    public static void removeOreBlock(Block block) {
        ORE_BLOCKS.remove(block);
    }

    private static void spawnActivationAnimation(ServerWorld world, BlockPos pos) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            // Circle of enchant particles
            for (int i = 0; i < 16; i++) {
                double angle = (i / 16.0) * Math.PI * 2;
                double px = x + Math.cos(angle) * 0.8;
                double pz = z + Math.sin(angle) * 0.8;
                world.spawnParticles(ParticleTypes.ENCHANT, px, y, pz, 1, 0.05, 0.05, 0.05, 0.15);
            }

            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_ACTIVATE, net.minecraft.sound.SoundCategory.BLOCKS, 0.8f, 1.5f);
        } catch (Exception e) {
            LOGGER.debug("Activation animation error: {}", e.getMessage());
        }
    }

    private static void spawnCompletionAnimation(ServerWorld world, BlockPos pos, int blocksDestroyed) {
        try {
            double x = pos.getX() + 0.5;
            double y = pos.getY() + 0.5;
            double z = pos.getZ() + 0.5;

            int particleCount = Math.min(blocksDestroyed * 3, 50);

            // Sphere burst of enchant particles
            for (int i = 0; i < particleCount; i++) {
                double angle1 = Math.random() * Math.PI * 2;
                double angle2 = Math.random() * Math.PI;
                double radius = 0.3 + Math.random() * 0.5;

                double px = x + Math.cos(angle1) * Math.sin(angle2) * radius;
                double py = y + Math.cos(angle2) * radius;
                double pz = z + Math.sin(angle1) * Math.sin(angle2) * radius;

                world.spawnParticles(ParticleTypes.ENCHANT, px, py, pz, 1, 0.05, 0.05, 0.05, 0.2);
            }

            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_BEACON_DEACTIVATE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.2f);
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 0.8f, 2.0f);
        } catch (Exception e) {
            LOGGER.debug("Completion animation error: {}", e.getMessage());
        }
    }

    private static void spawnParticleTrail(ServerWorld world, BlockPos start, BlockPos end, int blockIndex, int totalBlocks) {
        double startX = start.getX() + 0.5;
        double startY = start.getY() + 0.5;
        double startZ = start.getZ() + 0.5;

        double endX = end.getX() + 0.5;
        double endY = end.getY() + 0.5;
        double endZ = end.getZ() + 0.5;

        double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2) + Math.pow(endZ - startZ, 2));
        int steps = (int) (distance * 2);

        for (int i = 0; i < steps; i++) {
            double progress = (double) i / steps;
            double x = startX + (endX - startX) * progress;
            double y = startY + (endY - startY) * progress;
            double z = startZ + (endZ - startZ) * progress;

            // Rainbow/enchant particles along the trail
            world.spawnParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0.05, 0.05, 0.05, 0.1);
        }
    }

    private static void spawnBlockBreakIndicator(ServerWorld world, BlockPos pos, int blockIndex, int totalBlocks) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 1.5;
        double z = pos.getZ() + 0.5;

        // Progress indicator - particles form a number or progress bar
        int remainingBlocks = totalBlocks - blockIndex - 1;

        for (int i = 0; i < Math.min(remainingBlocks, 5); i++) {
            double offset = i * 0.3;
            world.spawnParticles(ParticleTypes.ENCHANT, x + offset, y, z, 1, 0.1, 0.1, 0.1, 0.15);
        }
    }

    private static void playSoundEffect(ServerWorld world, BlockPos pos) {
        try {
            // Primary break sound
            world.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_STONE_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 0.9f + (float) Math.random() * 0.2f);

            // Secondary sound with 50% chance
            if (Math.random() > 0.5) {
                world.playSound(null, pos, net.minecraft.sound.SoundEvents.ENTITY_ITEM_PICKUP, net.minecraft.sound.SoundCategory.PLAYERS, 0.6f, 1.2f + (float) Math.random() * 0.3f);
            }
        } catch (Exception e) {
            LOGGER.debug("Sound effect error: {}", e.getMessage());
        }
    }

    private static void sendVeinChat(ServerPlayerEntity player, String message) {
        ServerPlayNetworking.send(player, new ChatAnimatedPayload("VEIN", "VeinMiner", message));
    }
}
