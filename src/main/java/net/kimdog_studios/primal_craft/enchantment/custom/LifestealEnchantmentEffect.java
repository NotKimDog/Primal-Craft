package net.kimdog_studios.primal_craft.enchantment.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record LifestealEnchantmentEffect() implements EnchantmentEntityEffect {
    public static final MapCodec<LifestealEnchantmentEffect> CODEC = MapCodec.unit(LifestealEnchantmentEffect::new);

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        if(!(user instanceof LivingEntity living)) return;

        float healAmount = switch(level) {
            case 1 -> 2.0f;
            case 2 -> 4.0f;
            default -> 1.0f * level; // fallback scaling
        };

        living.heal(healAmount);
    }

    @Override
    public MapCodec<? extends EnchantmentEntityEffect> getCodec() {
        return CODEC;
    }
}
