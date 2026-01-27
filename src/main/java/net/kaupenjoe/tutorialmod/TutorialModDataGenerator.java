package net.kaupenjoe.tutorialmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.kaupenjoe.tutorialmod.datagen.ModRecipeProvider;
import net.kaupenjoe.tutorialmod.enchantment.ModEnchantments;
import net.kaupenjoe.tutorialmod.trim.ModTrimMaterials;
import net.kaupenjoe.tutorialmod.trim.ModTrimPatterns;
import net.kaupenjoe.tutorialmod.world.ModConfiguredFeatures;
import net.kaupenjoe.tutorialmod.world.ModPlacedFeatures;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Advanced data generator entry point for KimDog's SMP Mod.
 * Consolidated architecture - all datagen logic is centralized in DatagenHelper
 * Only ModRecipeProvider is registered as it uses DatagenHelper configuration
 *
 * @author KimDog Studios
 * @version 1.21.X
 */
public class TutorialModDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		long startTime = System.currentTimeMillis();
		TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
		TutorialMod.LOGGER.info("║  Starting Data Generation for {}                    ║", TutorialMod.MOD_ID);
		TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		int successCount = 0;
		int totalProviders = 1;

		// Only register the consolidated ModRecipeProvider
		// All other datagen logic is now centralized in DatagenHelper
		try {
			pack.addProvider(ModRecipeProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Recipe Generator [Recipes]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Recipe Generator - {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		int failCount = totalProviders - successCount;

		// Summary statistics
		TutorialMod.LOGGER.info("┌────────────────────────────────────────────────────────────┐");
		TutorialMod.LOGGER.info("│  Provider Registration Summary:                             │");
		TutorialMod.LOGGER.info("│    ✓ Total Providers: {}                                    │", totalProviders);
		TutorialMod.LOGGER.info("│    ✓ Registered Successfully: {}                            │", successCount);
		TutorialMod.LOGGER.info("│    ✗ Failed: {}                                             │", failCount);
		TutorialMod.LOGGER.info("│    ⏱  Time Taken: {}ms                                      │", elapsed);
		TutorialMod.LOGGER.info("└────────────────────────────────────────────────────────────┘");

		if (failCount > 0) {
			TutorialMod.LOGGER.warn("⚠ Some data generators failed! Check above for details.");
		} else {
			TutorialMod.LOGGER.info("✅ All data generators registered successfully!");
		}

		// Pretty-print all registered blocks and items for quick visibility
		var blockIds = Registries.BLOCK.stream()
				.map(Registries.BLOCK::getId)
				.filter(id -> id.getNamespace().equals(TutorialMod.MOD_ID))
				.sorted()
				.toList();
		logRegistryState("Registered Blocks", blockIds);

		var itemIds = Registries.ITEM.stream()
				.map(Registries.ITEM::getId)
				.filter(id -> id.getNamespace().equals(TutorialMod.MOD_ID))
				.sorted()
				.toList();
		logRegistryState("Registered Items", itemIds);

		TutorialMod.LOGGER.info("└─ End of registry listing");
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		long startTime = System.currentTimeMillis();
		TutorialMod.LOGGER.info("Building custom registries...");

		int registryCount = 0;

		try {
			// Armor trims
			registryBuilder.addRegistry(RegistryKeys.TRIM_MATERIAL, ModTrimMaterials::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Trim Materials");

			registryBuilder.addRegistry(RegistryKeys.TRIM_PATTERN, ModTrimPatterns::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Trim Patterns");

			// Enchantments
			registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, ModEnchantments::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Enchantments");

			// World generation
			registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Configured Features");

			registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Placed Features");
		} catch (Exception e) {
			TutorialMod.LOGGER.error("Failed to build custom registries: {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		TutorialMod.LOGGER.info("Built {} custom registries in {}ms", registryCount, elapsed);
	}

	/**
	 * Log registry state for visibility
	 */
	private void logRegistryState(String title, java.util.List<Identifier> ids) {
		TutorialMod.LOGGER.info("┌─ {} ({}):", title, ids.size());
		ids.forEach(id -> TutorialMod.LOGGER.info("│   ✓ {}", id));
		TutorialMod.LOGGER.info("│");
	}
}
