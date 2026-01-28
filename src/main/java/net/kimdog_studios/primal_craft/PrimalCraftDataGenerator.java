package net.kimdog_studios.primal_craft;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.kimdog_studios.primal_craft.datagen.*;
import net.kimdog_studios.primal_craft.enchantment.ModEnchantments;
import net.kimdog_studios.primal_craft.trim.ModTrimMaterials;
import net.kimdog_studios.primal_craft.trim.ModTrimPatterns;
import net.kimdog_studios.primal_craft.world.ModConfiguredFeatures;
import net.kimdog_studios.primal_craft.world.ModPlacedFeatures;
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
public class PrimalCraftDataGenerator implements DataGeneratorEntrypoint {

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		long startTime = System.currentTimeMillis();
		PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
		PrimalCraft.LOGGER.info("║  Starting Data Generation for {}                    ║", PrimalCraft.MOD_ID);
		PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		int successCount = 0;
		int totalProviders = 6;

		// Register providers in dependency order with error handling
		// Tags must be first as other providers depend on them
		try {
			pack.addProvider(ModBlockTagProvider::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Block Tags [Tags]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Block Tags - {}", e.getMessage(), e);
		}

		try {
			pack.addProvider(ModItemTagProvider::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Item Tags [Tags]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Item Tags - {}", e.getMessage(), e);
		}

		// Loot tables depend on block tags
		try {
			pack.addProvider(ModLootTableProvider::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Loot Tables [Loot]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Loot Tables - {}", e.getMessage(), e);
		}

		// Models can be generated independently
		try {
			pack.addProvider(ModModelProvider::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Models & Block States [Models]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Models & Block States - {}", e.getMessage(), e);
		}

		// Recipes depend on item tags
		try {
			pack.addProvider(ModRecipeProvider::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Recipes [Recipes]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Recipes - {}", e.getMessage(), e);
		}

		// Registry data (trims, enchantments, world gen)
		try {
			pack.addProvider(ModRegistryDataGenerator::new);
			PrimalCraft.LOGGER.info("  ✓ Registered: Registry Data [Registry]");
			successCount++;
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to register: Registry Data - {}", e.getMessage(), e);
		}

		// Generate automated wiki documentation
		try {
			PrimalCraft.LOGGER.info("  📚 Generating automated wiki...");
			WikiGenerator.generateWiki();
			PrimalCraft.LOGGER.info("  ✓ Wiki generation complete");
		} catch (Exception e) {
			PrimalCraft.LOGGER.error("  ✗ Failed to generate wiki - {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		int failCount = totalProviders - successCount;

		// Summary statistics
		PrimalCraft.LOGGER.info("┌────────────────────────────────────────────────────────────┐");
		PrimalCraft.LOGGER.info("│  Provider Registration Summary:                            │");
		PrimalCraft.LOGGER.info("│    • Total Providers: {}                                    │", totalProviders);
		PrimalCraft.LOGGER.info("│    • Registered Successfully: {}                            │", successCount);
		PrimalCraft.LOGGER.info("│    • Failed: {}                                             │", failCount);
		PrimalCraft.LOGGER.info("│    • Time Taken: {}ms                                      │", elapsed);
		PrimalCraft.LOGGER.info("└────────────────────────────────────────────────────────────┘");

		if (failCount > 0) {
			PrimalCraft.LOGGER.warn("⚠ Some providers failed to register! Check logs above for details.");
		}

		// Pretty-print all registered blocks and items for quick visibility
		var blockIds = Registries.BLOCK.stream()
				.map(Registries.BLOCK::getId)
				.filter(id -> id.getNamespace().equals(PrimalCraft.MOD_ID))
				.sorted()
				.toList();
		logRegistryState("Registered Blocks", blockIds);

		var itemIds = Registries.ITEM.stream()
				.map(Registries.ITEM::getId)
				.filter(id -> id.getNamespace().equals(PrimalCraft.MOD_ID))
				.sorted()
				.toList();
		logRegistryState("Registered Items", itemIds);

		PrimalCraft.LOGGER.info("└─ End of registry listing");
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		long startTime = System.currentTimeMillis();
		PrimalCraft.LOGGER.info("Building custom registries...");

		int registryCount = 0;

		try {
			// Armor trims
			registryBuilder.addRegistry(RegistryKeys.TRIM_MATERIAL, ModTrimMaterials::bootstrap);
			registryCount++;
			PrimalCraft.LOGGER.info("  ✓ Trim Materials");

			registryBuilder.addRegistry(RegistryKeys.TRIM_PATTERN, ModTrimPatterns::bootstrap);
			registryCount++;
			PrimalCraft.LOGGER.info("  ✓ Trim Patterns");

			// Enchantments
			registryBuilder.addRegistry(RegistryKeys.ENCHANTMENT, ModEnchantments::bootstrap);
			registryCount++;
			PrimalCraft.LOGGER.info("  ✓ Enchantments");

			// World generation features
			registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
			registryCount++;
			PrimalCraft.LOGGER.info("  ✓ Configured Features");

			registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, ModPlacedFeatures::bootstrap);
			registryCount++;
			PrimalCraft.LOGGER.info("  ✓ Placed Features");

		} catch (Exception e) {
			PrimalCraft.LOGGER.error("Failed to build registries: {}", e.getMessage(), e);
		}

		long elapsed = System.currentTimeMillis() - startTime;
		PrimalCraft.LOGGER.info("Built {} custom registries in {}ms", registryCount, elapsed);
	}

	@Override
	public String getEffectiveModId() {
		return PrimalCraft.MOD_ID;
	}

	private void logRegistryState(String title, java.util.List<Identifier> ids) {
		PrimalCraft.LOGGER.info("├─ {} ({}):", title, ids.size());
		ids.forEach(id -> PrimalCraft.LOGGER.info("│   • {}", id));
		var seen = new HashSet<Identifier>();
		var duplicates = ids.stream().filter(id -> !seen.add(id)).toList();
		if (!duplicates.isEmpty()) {
			PrimalCraft.LOGGER.warn("│   ⚠ Duplicates in {}: {}", title, duplicates);
		}
	}
}
