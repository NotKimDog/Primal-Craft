package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.util.ModTags;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;

import java.util.concurrent.CompletableFuture;

/**
 * FULLY AUTOMATED Block Tag Provider
 * Uses DatagenHelper to automatically discover and categorize all blocks via reflection.
 * No manual block additions needed!
 *
 * @author KimDog Studios
 */
public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        long startTime = System.currentTimeMillis();
        TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        TutorialMod.LOGGER.info("║  STARTING BLOCK TAG GENERATION                             ║");
        TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        int tagsCreated = 0;

        // ===== MINEABLE WITH PICKAXE (Auto-detect ores, blocks, stone-like materials) =====
        var pickaxeBuilder = valueLookupBuilder(BlockTags.PICKAXE_MINEABLE);
        int pickaxeCount = 0;

        // Auto-add all ores
        pickaxeCount += DatagenHelper.getOres().size();
        DatagenHelper.getOres().forEach(entry -> pickaxeBuilder.add(entry.block()));
        TutorialMod.LOGGER.debug("  • Added {} ore blocks to pickaxe mineable", DatagenHelper.getOres().size());

        // Auto-add blocks containing these keywords
        var pickaxeExtra = DatagenHelper.getBlocksContaining("block", "ore", "lamp", "door", "trapdoor", "button",
                                          "pressure_plate", "fence", "wall", "stairs", "slab")
            .stream()
            .filter(entry -> !entry.name().contains("driftwood")) // Exclude wood-based
            .filter(entry -> !entry.name().contains("leaves"))
            .filter(entry -> !entry.name().contains("sapling"))
            .toList();
        pickaxeCount += pickaxeExtra.size();
        pickaxeExtra.forEach(entry -> pickaxeBuilder.add(entry.block()));
        TutorialMod.LOGGER.debug("  • Added {} additional blocks to pickaxe mineable", pickaxeExtra.size());

        TutorialMod.LOGGER.info("  ✓ Pickaxe mineable blocks: {} total", pickaxeCount);
        tagsCreated++;

        // ===== MINEABLE WITH AXE (Logs, planks, wood-based blocks) =====
        var axeBuilder = valueLookupBuilder(BlockTags.AXE_MINEABLE);
        int axeCount = 0;

        axeCount += DatagenHelper.getLogs().size();
        DatagenHelper.getLogs().forEach(entry -> axeBuilder.add(entry.block()));
        axeCount += DatagenHelper.getPlanks().size();
        DatagenHelper.getPlanks().forEach(entry -> axeBuilder.add(entry.block()));

        var axeExtra = DatagenHelper.getBlocksContaining("driftwood").stream()
            .filter(entry -> !entry.name().contains("leaves"))
            .filter(entry -> !entry.name().contains("sapling"))
            .toList();
        axeCount += axeExtra.size();
        axeExtra.forEach(entry -> axeBuilder.add(entry.block()));

        TutorialMod.LOGGER.info("  ✓ Axe mineable blocks: {} total", axeCount);
        tagsCreated++;

        // ===== TOOL REQUIREMENTS (Auto-detect based on names) =====
        var ironToolBuilder = valueLookupBuilder(BlockTags.NEEDS_IRON_TOOL);

        var ironBlocks = DatagenHelper.getBlocksContaining("deepslate");
        ironBlocks.forEach(entry -> {
            ironToolBuilder.add(entry.block());
        });

        TutorialMod.LOGGER.info("  ✓ Iron tool required blocks: {}", ironBlocks.size());
        tagsCreated++;

        var diamondToolBuilder = valueLookupBuilder(BlockTags.NEEDS_DIAMOND_TOOL);
        var diamondBlocks = DatagenHelper.getBlocksContaining("magic");
        diamondBlocks.forEach(entry -> {
            diamondToolBuilder.add(entry.block());
        });

        TutorialMod.LOGGER.info("  ✓ Diamond tool required blocks: {}", diamondBlocks.size());
        tagsCreated++;

        // ===== FENCES, WALLS, GATES (Fully Automated) =====
        var fencesBuilder = valueLookupBuilder(BlockTags.FENCES);
        var fenceBlocks = DatagenHelper.getFences().stream()
            .filter(entry -> !entry.name().contains("gate"))
            .toList();
        fenceBlocks.forEach(entry -> fencesBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Fences: {} blocks", fenceBlocks.size());
        tagsCreated++;

        var fenceGatesBuilder = valueLookupBuilder(BlockTags.FENCE_GATES);
        var gateBlocks = DatagenHelper.getBlocksContaining("fence_gate");
        gateBlocks.forEach(entry -> {
            fenceGatesBuilder.add(entry.block());
        });
        TutorialMod.LOGGER.info("  ✓ Fence gates: {} blocks", gateBlocks.size());
        tagsCreated++;

        var wallsBuilder = valueLookupBuilder(BlockTags.WALLS);
        var wallBlocks = DatagenHelper.getWalls();
        wallBlocks.forEach(entry -> wallsBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Walls: {} blocks", wallBlocks.size());
        tagsCreated++;

        // ===== LOGS (Auto-detect all log types) =====
        var logsBuilder = valueLookupBuilder(BlockTags.LOGS_THAT_BURN);
        var logBlocks = DatagenHelper.getLogs();
        logBlocks.forEach(entry -> logsBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Logs that burn: {} blocks", logBlocks.size());
        tagsCreated++;

        // ===== CUSTOM TOOL REQUIREMENT =====
        var pinkGarnetToolBuilder = valueLookupBuilder(ModTags.Blocks.NEEDS_PINK_GARNET_TOOL);
        var customBlocks = DatagenHelper.getBlocksContaining("magic");
        customBlocks.forEach(entry -> {
            pinkGarnetToolBuilder.add(entry.block());
        });
        pinkGarnetToolBuilder.addTag(BlockTags.NEEDS_IRON_TOOL);

        TutorialMod.LOGGER.info("  ✓ Pink Garnet tool required blocks: {}", customBlocks.size());
        tagsCreated++;

        long elapsed = System.currentTimeMillis() - startTime;
        TutorialMod.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        TutorialMod.LOGGER.info("║  BLOCK TAG GENERATION COMPLETE                             ║");
        TutorialMod.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        TutorialMod.LOGGER.info("  📊 Total Tags Created: {}", tagsCreated);
        TutorialMod.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
    }
}
