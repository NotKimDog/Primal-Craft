package net.kimdog_studios.primal_craft.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public record FrostbiteEnchantmentEffect() implements EnchantmentEntityEffect {
    public static final MapCodec<FrostbiteEnchantmentEffect> CODEC = MapCodec.unit(FrostbiteEnchantmentEffect::new);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        // The EnchantmentEffectContext API in this environment doesn't expose a direct getTarget() method.
        // Use the provided position (pos) to locate nearby LivingEntity targets and apply the frost effect.
        if(world == null) return;

        Box area = new Box(pos.x - 1.0, pos.y - 1.0, pos.z - 1.0, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0);
        List<LivingEntity> nearby = world.getEntitiesByClass(LivingEntity.class, area, e -> e.isAlive());
        if(nearby.isEmpty()) return;

        // Apply to the closest entity (primary target)
        LivingEntity primary = nearby.get(0);
        int duration = 40 * Math.max(1, level); // ticks
        primary.addStatusEffect(new net.minecraft.entity.effect.StatusEffectInstance(net.minecraft.entity.effect.StatusEffects.SLOWNESS, duration, Math.max(0, level - 1)));

        // Spawn frost particles around the primary target
        BlockPos bpos = primary.getBlockPos();
        world.spawnParticles(ParticleTypes.ITEM_SNOWBALL, bpos.getX() + 0.5, bpos.getY() + 1.0, bpos.getZ() + 0.5, 8, 0.3, 0.5, 0.3, 0.0);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
