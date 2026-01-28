package net.kimdog_studios.primal_craft.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Box;

import java.util.List;

public record ChainLightningEnchantmentEffect() implements EnchantmentEntityEffect {
    public static final MapCodec<ChainLightningEnchantmentEffect> CODEC = MapCodec.unit(ChainLightningEnchantmentEffect::new);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if(world == null) return;

        // Find the nearest living entity around the hit position
        List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, new Box(pos.x-1, pos.y-1, pos.z-1, pos.x+1, pos.y+1, pos.z+1), e -> e.isAlive());
        if(nearby.isEmpty()) return;
        LivingEntity primary = nearby.get(0);

        // Strike the initial target
        EntityType.LIGHTNING_BOLT.spawn(world, primary.getBlockPos(), SpawnReason.TRIGGERED);

        // Find nearby entities and strike up to `level` additional targets
        int remaining = Math.max(0, level - 1); // level 1: just primary; level 2+: additional
        List<LivingEntity> others = world.getEntitiesByClass(LivingEntity.class, new Box(primary.getX()-6, primary.getY()-3, primary.getZ()-6, primary.getX()+6, primary.getY()+3, primary.getZ()+6), e -> e != primary && e.isAlive());
        for(LivingEntity le : others) {
            if(remaining-- <= 0) break;
            EntityType.LIGHTNING_BOLT.spawn(world, le.getBlockPos(), SpawnReason.TRIGGERED);
        }
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
