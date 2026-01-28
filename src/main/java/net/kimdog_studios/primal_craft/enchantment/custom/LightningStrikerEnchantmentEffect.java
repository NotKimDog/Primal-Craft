package net.kimdog_studios.primal_craft.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.kimdog_studios.primal_craft.enchantment.LightningTaskManager;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record LightningStrikerEnchantmentEffect() implements EnchantmentEntityEffect {
    public static final MapCodec<LightningStrikerEnchantmentEffect> CODEC = MapCodec.unit(LightningStrikerEnchantmentEffect::new);

    // Duration per enchantment level (ticks)
    private static final int DURATION_TICKS_PER_LEVEL = 80; // 4 seconds per level
    private static final int INTERVAL_TICKS = 10; // strike every 10 ticks (0.5s)

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if(level <= 0) level = 1;

        // Immediate initial strike at the hit position (not the user) to avoid hitting the attacker
        BlockPos strikePos = new BlockPos((int)Math.floor(pos.x), (int)Math.floor(pos.y), (int)Math.floor(pos.z));
        EntityType.LIGHTNING_BOLT.spawn(world, strikePos, SpawnReason.TRIGGERED);

        // Enqueue a timed task to chain lightning for a set duration, excluding the attacker
        int duration = DURATION_TICKS_PER_LEVEL * level;
        UUID excluded = user != null ? user.getUuid() : null;
        LightningTaskManager.addTask(world, pos, level, duration, INTERVAL_TICKS, excluded);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
