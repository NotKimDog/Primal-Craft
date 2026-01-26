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
        TutorialMod.LOGGER.info("Starting automated block tag generation...");

        // ===== MINEABLE WITH PICKAXE (Auto-detect ores, blocks, stone-like materials) =====
        var pickaxeBuilder = valueLookupBuilder(BlockTags.PICKAXE_MINEABLE);

        // Auto-add all ores
        DatagenHelper.getOres().forEach(entry -> pickaxeBuilder.add(entry.block()));

        // Auto-add blocks containing these keywords
        DatagenHelper.getBlocksContaining("block", "ore", "lamp", "door", "trapdoor", "button",
                                          "pressure_plate", "fence", "wall", "stairs", "slab")
            .stream()
            .filter(entry -> !entry.name().contains("driftwood")) // Exclude wood-based
            .filter(entry -> !entry.name().contains("leaves"))
            .filter(entry -> !entry.name().contains("sapling"))
            .forEach(entry -> pickaxeBuilder.add(entry.block()));

        TutorialMod.LOGGER.info("  ✓ Pickaxe mineable blocks");

        // ===== MINEABLE WITH AXE (Logs, planks, wood-based blocks) =====
        var axeBuilder = valueLookupBuilder(BlockTags.AXE_MINEABLE);

        DatagenHelper.getLogs().forEach(entry -> axeBuilder.add(entry.block()));
        DatagenHelper.getPlanks().forEach(entry -> axeBuilder.add(entry.block()));
        DatagenHelper.getBlocksContaining("driftwood").stream()
            .filter(entry -> !entry.name().contains("leaves"))
            .filter(entry -> !entry.name().contains("sapling"))
            .forEach(entry -> axeBuilder.add(entry.block()));

        TutorialMod.LOGGER.info("  ✓ Axe mineable blocks");

        // ===== TOOL REQUIREMENTS (Auto-detect based on names) =====
        var ironToolBuilder = valueLookupBuilder(BlockTags.NEEDS_IRON_TOOL);

        DatagenHelper.getBlocksContaining("deepslate").forEach(entry -> {
            ironToolBuilder.add(entry.block());
        });

        TutorialMod.LOGGER.info("  ✓ Iron tool required blocks");

        var diamondToolBuilder = valueLookupBuilder(BlockTags.NEEDS_DIAMOND_TOOL);
        DatagenHelper.getBlocksContaining("magic").forEach(entry -> {
            diamondToolBuilder.add(entry.block());
        });

        TutorialMod.LOGGER.info("  ✓ Diamond tool required blocks");

        // ===== FENCES, WALLS, GATES (Fully Automated) =====
        var fencesBuilder = valueLookupBuilder(BlockTags.FENCES);
        DatagenHelper.getFences().stream()
            .filter(entry -> !entry.name().contains("gate"))
            .forEach(entry -> fencesBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Fences: {} blocks", DatagenHelper.getFences().size());

        var fenceGatesBuilder = valueLookupBuilder(BlockTags.FENCE_GATES);
        DatagenHelper.getBlocksContaining("fence_gate").forEach(entry -> {
            fenceGatesBuilder.add(entry.block());
        });
        TutorialMod.LOGGER.info("  ✓ Fence gates");

        var wallsBuilder = valueLookupBuilder(BlockTags.WALLS);
        DatagenHelper.getWalls().forEach(entry -> wallsBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Walls: {} blocks", DatagenHelper.getWalls().size());

        // ===== LOGS (Auto-detect all log types) =====
        var logsBuilder = valueLookupBuilder(BlockTags.LOGS_THAT_BURN);
        DatagenHelper.getLogs().forEach(entry -> logsBuilder.add(entry.block()));
        TutorialMod.LOGGER.info("  ✓ Logs that burn: {} blocks", DatagenHelper.getLogs().size());

        // ===== CUSTOM TOOL REQUIREMENT =====
        var pinkGarnetToolBuilder = valueLookupBuilder(ModTags.Blocks.NEEDS_PINK_GARNET_TOOL);
        DatagenHelper.getBlocksContaining("magic").forEach(entry -> {
            pinkGarnetToolBuilder.add(entry.block());
        });
        pinkGarnetToolBuilder.addTag(BlockTags.NEEDS_IRON_TOOL);

        TutorialMod.LOGGER.info("  ✓ Pink Garnet tool required blocks");

        long elapsed = System.currentTimeMillis() - startTime;
        TutorialMod.LOGGER.info("Automated block tag generation completed in {}ms", elapsed);
    }
}
