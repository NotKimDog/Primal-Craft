package net.kimdog_studios.primal_craft.world.tree;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.world.ModConfiguredFeatures;
import net.minecraft.block.SaplingGenerator;

import java.util.Optional;

public class ModSaplingGenerators {
    public static final SaplingGenerator DRIFTWOOD = new SaplingGenerator(PrimalCraft.MOD_ID + ":driftwood",
            Optional.empty(), Optional.of(ModConfiguredFeatures.DRIFTWOOD_KEY), Optional.empty());
}
