package net.kaupenjoe.tutorialmod;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.kaupenjoe.tutorialmod.datagen.*;
import net.kaupenjoe.tutorialmod.enchantment.ModEnchantments;
import net.kaupenjoe.tutorialmod.trim.ModTrimMaterials;
import net.kaupenjoe.tutorialmod.trim.ModTrimPatterns;
import net.kaupenjoe.tutorialmod.world.ModConfiguredFeatures;
import net.kaupenjoe.tutorialmod.world.ModPlacedFeatures;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;

/**
 * Advanced data generator entry point for KimDog's SMP Mod.
 * Features:
 * - Automated provider registration with dependency ordering
 * - Performance tracking and validation
 * - Robust error handling with detailed logging
 * - Professional output formatting
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
		int totalProviders = 6;

		// Register providers in dependency order with error handling
		// Tags must be first as other providers depend on them
		try {
			pack.addProvider(ModBlockTagProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Block Tags [Tags]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Block Tags - {}", e.getMessage(), e);
		}

		try {
			pack.addProvider(ModItemTagProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Item Tags [Tags]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Item Tags - {}", e.getMessage(), e);
		}

		// Loot tables depend on block tags
		try {
			pack.addProvider(ModLootTableProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Loot Tables [Loot]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Loot Tables - {}", e.getMessage(), e);
		}

		// Models can be generated independently
		try {
			pack.addProvider(ModModelProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Models & Block States [Models]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Models & Block States - {}", e.getMessage(), e);
		}

		// Recipes depend on item tags
		try {
			pack.addProvider(ModRecipeProvider::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Recipes [Recipes]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Recipes - {}", e.getMessage(), e);
		}

		// Registry data (trims, enchantments, world gen)
		try {
			pack.addProvider(ModRegistryDataGenerator::new);
			TutorialMod.LOGGER.info("  ✓ Registered: Registry Data [Registry]");
			successCount++;
		} catch (Exception e) {
			TutorialMod.LOGGER.error("  ✗ Failed to register: Registry Data - {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		int failCount = totalProviders - successCount;

		// Summary statistics
		TutorialMod.LOGGER.info("┌────────────────────────────────────────────────────────────┐");
		TutorialMod.LOGGER.info("│  Provider Registration Summary:                            │");
		TutorialMod.LOGGER.info("│    • Total Providers: {}                                    │", totalProviders);
		TutorialMod.LOGGER.info("│    • Registered Successfully: {}                            │", successCount);
		TutorialMod.LOGGER.info("│    • Failed: {}                                             │", failCount);
		TutorialMod.LOGGER.info("│    • Time Taken: {}ms                                      │", elapsed);
		TutorialMod.LOGGER.info("└────────────────────────────────────────────────────────────┘");

		if (failCount > 0) {
			TutorialMod.LOGGER.warn("⚠ Some providers failed to register! Check logs above for details.");
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

			// World generation features
			registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Configured Features");

			registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
			registryCount++;
			TutorialMod.LOGGER.info("  ✓ Placed Features");

		} catch (Exception e) {
			TutorialMod.LOGGER.error("Failed to build registries: {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		TutorialMod.LOGGER.info("Built {} custom registries in {}ms", registryCount, elapsed);
	}

	@Override
	public String getEffectiveModId() {
		return TutorialMod.MOD_ID;
	}

	private void logRegistryState(String title, java.util.List<Identifier> ids) {
		TutorialMod.LOGGER.info("├─ {} ({}):", title, ids.size());
		ids.forEach(id -> TutorialMod.LOGGER.info("│   • {}", id));
		var seen = new HashSet<Identifier>();
		var duplicates = ids.stream().filter(id -> !seen.add(id)).toList();
		if (!duplicates.isEmpty()) {
			TutorialMod.LOGGER.warn("│   ⚠ Duplicates in {}: {}", title, duplicates);
		}
	}
}
