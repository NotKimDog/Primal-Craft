package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.block.ModBlocks;
import net.kaupenjoe.tutorialmod.block.custom.CauliflowerCropBlock;
import net.kaupenjoe.tutorialmod.block.custom.HoneyBerryBushBlock;
import net.kaupenjoe.tutorialmod.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * FULLY AUTOMATED Loot Table Provider
 * Uses DatagenHelper to automatically discover blocks and apply appropriate drop logic.
 * Automatically handles: ores, slabs, doors, leaves, stairs, and regular blocks.
 *
 * @author KimDog Studios
 */
public class ModLootTableProvider extends FabricBlockLootTableProvider {
    public ModLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        long startTime = System.currentTimeMillis();
        TutorialMod.LOGGER.info("Starting automated loot table generation...");

        RegistryWrapper.Impl<Enchantment> impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        int generated = 0;

        // ===== AUTO-DETECT AND HANDLE ALL BLOCKS =====
        for (var entry : DatagenHelper.getAllBlocks()) {
            Block block = entry.block();
            String name = entry.name();

            // Special handling for specific block types
            if (name.contains("_ore")) {
                // Ores drop raw materials with fortune
                if (name.contains("deepslate")) {
                    addDrop(block, multipleOreDrops(block, ModItems.RAW_PINK_GARNET, 3, 7));
                } else if (name.contains("end")) {
                    addDrop(block, multipleOreDrops(block, ModItems.RAW_PINK_GARNET, 4, 9));
                } else if (name.contains("nether")) {
                    addDrop(block, multipleOreDrops(block, ModItems.RAW_PINK_GARNET, 3, 8));
                } else {
                    addDrop(block, oreDrops(block, ModItems.RAW_PINK_GARNET));
                }
                generated++;
            } else if (name.contains("slab")) {
                // Slabs double drop when silk touched
                addDrop(block, slabDrops(block));
                generated++;
            } else if (name.contains("door") && !name.contains("trapdoor")) {
                // Doors drop 1 item (not 2)
                addDrop(block, doorDrops(block));
                generated++;
            } else if (name.contains("leaves")) {
                // Leaves drop saplings with chance
                if (name.contains("driftwood")) {
                    addDrop(block, leavesDrops(block, ModBlocks.DRIFTWOOD_SAPLING, 0.0625f));
                } else {
                    addDrop(block, leavesDrops(block, block, 0.05f)); // Default
                }
                generated++;
            } else if (name.equals("cauliflower_crop")) {
                // Custom crop handling
                BlockStatePropertyLootCondition.Builder builder = BlockStatePropertyLootCondition.builder(ModBlocks.CAULIFLOWER_CROP)
                        .properties(StatePredicate.Builder.create().exactMatch(CauliflowerCropBlock.AGE, CauliflowerCropBlock.MAX_AGE));
                addDrop(block, cropDrops(block, ModItems.CAULIFLOWER, ModItems.CAULIFLOWER_SEEDS, builder));
                generated++;
            } else if (name.equals("honey_berry_bush")) {
                // Custom berry bush handling
                addDrop(ModBlocks.HONEY_BERRY_BUSH,
                    blk -> this.applyExplosionDecay(
                        blk, LootTable.builder().pool(LootPool.builder().conditionally(
                                BlockStatePropertyLootCondition.builder(ModBlocks.HONEY_BERRY_BUSH)
                                    .properties(StatePredicate.Builder.create().exactMatch(HoneyBerryBushBlock.AGE, 3))
                            )
                            .with(ItemEntry.builder(ModItems.HONEY_BERRIES))
                            .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0F, 3.0F)))
                            .apply(ApplyBonusLootFunction.uniformBonusCount(impl.getOrThrow(Enchantments.FORTUNE)))
                        ).pool(LootPool.builder().conditionally(
                                BlockStatePropertyLootCondition.builder(ModBlocks.HONEY_BERRY_BUSH)
                                    .properties(StatePredicate.Builder.create().exactMatch(HoneyBerryBushBlock.AGE, 2))
                            )
                            .with(ItemEntry.builder(ModItems.HONEY_BERRIES))
                            .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0F, 2.0F)))
                            .apply(ApplyBonusLootFunction.uniformBonusCount(impl.getOrThrow(Enchantments.FORTUNE))))));
                generated++;
            } else if (!name.contains("sapling")) {
                // Regular blocks drop themselves (skip saplings as they're handled by leaves)
                addDrop(block);
                generated++;
            }
        }

        long elapsed = System.currentTimeMillis() - startTime;
        TutorialMod.LOGGER.info("Automated loot table generation completed in {}ms", elapsed);
        TutorialMod.LOGGER.info("  ✓ Generated {} loot tables automatically", generated);
    }

    /**
     * Helper for ores that drop multiple items with fortune
     */
    public LootTable.Builder multipleOreDrops(Block drop, Item item, float minDrops, float maxDrops) {
        RegistryWrapper.Impl<Enchantment> impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, this.applyExplosionDecay(drop, ((LeafEntry.Builder<?>)
                ItemEntry.builder(item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(minDrops, maxDrops))))
                .apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))));
    }
}
