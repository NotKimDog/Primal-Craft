package net.kimdog_studios.primal_craft.enchantment;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Manages timed lightning chain tasks.
 * Tasks are executed on server ticks via ServerTickEvents.END_SERVER_TICK.
 */
public class LightningTaskManager {
    private static final List<LightningTask> TASKS = new ArrayList<>();
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> tick(server));
    }

    public static void addTask(ServerWorld world, Vec3d pos, int level, int durationTicks, int intervalTicks, UUID excludedPlayer) {
        synchronized (TASKS) {
            TASKS.add(new LightningTask(world.getRegistryKey(), pos, level, durationTicks, intervalTicks, excludedPlayer));
        }
    }

    private static void tick(MinecraftServer server) {
        synchronized (TASKS) {
            Iterator<LightningTask> it = TASKS.iterator();
            while (it.hasNext()) {
                LightningTask t = it.next();
                if (!t.tick(server)) {
                    it.remove();
                }
            }
        }
    }

    private static class LightningTask {
        // Use RegistryKey<World> to match ServerWorld#getRegistryKey and server.getWorld signature
        private final net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey;
        private final Vec3d pos;
        private int remainingTicks;
        private final int intervalTicks;
        private int counterTicks = 0;
        private final int level;
        private final UUID excludedPlayer;

        public LightningTask(net.minecraft.registry.RegistryKey<net.minecraft.world.World> worldKey, Vec3d pos, int level, int durationTicks, int intervalTicks, UUID excludedPlayer) {
            this.worldKey = worldKey;
            this.pos = pos;
            this.level = level;
            this.remainingTicks = durationTicks;
            this.intervalTicks = Math.max(1, intervalTicks);
            this.excludedPlayer = excludedPlayer;
        }

        public boolean tick(MinecraftServer server) {
            ServerWorld world = server.getWorld(worldKey);
            if (world == null) return false;

            if (remainingTicks <= 0) return false;

            counterTicks++;
            remainingTicks--;

            if (counterTicks >= intervalTicks) {
                counterTicks = 0;
                // Perform a chain strike: find nearby living entities and strike up to level targets
                List<LivingEntity> others = world.getEntitiesByClass(LivingEntity.class, new Box(pos.x - 6, pos.y - 3, pos.z - 6, pos.x + 6, pos.y + 3, pos.z + 6), e -> e.isAlive());
                int strikes = Math.max(1, level);
                int hitCount = 0;
                for (int i = 0; i < others.size() && hitCount < strikes; i++) {
                    LivingEntity le = others.get(i);
                    if (excludedPlayer != null && le.getUuid().equals(excludedPlayer)) continue; // skip attacker
                    EntityType.LIGHTNING_BOLT.spawn(world, le.getBlockPos(), net.minecraft.entity.SpawnReason.TRIGGERED);
                    hitCount++;
                }
            }

            return remainingTicks > 0;
        }
    }
}
