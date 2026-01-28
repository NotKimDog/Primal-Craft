package net.kimdog_studios.primal_craft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRegistryDataGenerator extends FabricDynamicRegistryProvider {
    public ModRegistryDataGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        long startTime = System.currentTimeMillis();
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  STARTING REGISTRY DATA GENERATION                         ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        int registriesLoaded = 0;

        // Armor trims
        try {
            var trimMaterials = registries.getOrThrow(RegistryKeys.TRIM_MATERIAL);
            entries.addAll(trimMaterials);
            PrimalCraft.LOGGER.info("  ✓ Trim Materials loaded");
            registriesLoaded++;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("  ⚠ Failed to load trim materials: {}", e.getMessage());
        }

        try {
            var trimPatterns = registries.getOrThrow(RegistryKeys.TRIM_PATTERN);
            entries.addAll(trimPatterns);
            PrimalCraft.LOGGER.info("  ✓ Trim Patterns loaded");
            registriesLoaded++;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("  ⚠ Failed to load trim patterns: {}", e.getMessage());
        }

        // Enchantments (add all - Minecraft handles missing ones)
        try {
            var enchantments = registries.getOrThrow(RegistryKeys.ENCHANTMENT);
            entries.addAll(enchantments);
            PrimalCraft.LOGGER.info("  ✓ Enchantments loaded");
            registriesLoaded++;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("  ⚠ Failed to load enchantments: {}", e.getMessage());
        }

        // World generation features
        try {
            var configuredFeatures = registries.getOrThrow(RegistryKeys.CONFIGURED_FEATURE);
            entries.addAll(configuredFeatures);
            PrimalCraft.LOGGER.info("  ✓ Configured Features loaded");
            registriesLoaded++;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("  ⚠ Failed to load configured features: {}", e.getMessage());
        }

        try {
            var placedFeatures = registries.getOrThrow(RegistryKeys.PLACED_FEATURE);
            entries.addAll(placedFeatures);
            PrimalCraft.LOGGER.info("  ✓ Placed Features loaded");
            registriesLoaded++;
        } catch (Exception e) {
            PrimalCraft.LOGGER.warn("  ⚠ Failed to load placed features: {}", e.getMessage());
        }

        long elapsed = System.currentTimeMillis() - startTime;
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  REGISTRY DATA GENERATION COMPLETE                         ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        PrimalCraft.LOGGER.info("  📊 Registry Groups Loaded: {}", registriesLoaded);
        PrimalCraft.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
    }

    @Override
    public String getName() {
        return "TutorialMod Registry Data";
    }
}
