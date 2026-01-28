package net.kimdog_studios.primal_craft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.util.ModTags;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;

import java.util.concurrent.CompletableFuture;

/**
 * FULLY AUTOMATED Item Tag Provider
 * Uses DatagenHelper to automatically discover and categorize all items via reflection.
 * No manual item additions needed - just register items in ModItems and they'll be tagged!
 *
 * @author KimDog Studios
 */
public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        long startTime = System.currentTimeMillis();
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  STARTING ITEM TAG GENERATION                              ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        int tagsCreated = 0;

        // ===== TRANSFORMABLE ITEMS (Custom tag) =====
        var transformableBuilder = valueLookupBuilder(ModTags.Items.TRANSFORMABLE_ITEMS);

        // Auto-add all items containing these keywords
        var transformableItems = DatagenHelper.getItemsContaining("pink_garnet", "stick");
        transformableItems.forEach(entry -> {
            transformableBuilder.add(entry.item());
        });
        PrimalCraft.LOGGER.debug("  • Added {} transformable items", transformableItems.size());

        // Add vanilla items
        transformableBuilder.add(Items.COAL, Items.APPLE);

        PrimalCraft.LOGGER.info("  ✓ Transformable items tag: {} items", transformableItems.size() + 2);
        tagsCreated++;

        // ===== REPAIR MATERIALS =====
        var repairMaterials = DatagenHelper.getItemsContaining("pink_garnet").stream()
            .filter(entry -> entry.name().equals("pink_garnet"))
            .toList();
        repairMaterials.forEach(entry -> {
            valueLookupBuilder(ModTags.Items.PINK_GARNET_REPAIR).add(entry.item());
        });

        PrimalCraft.LOGGER.info("  ✓ Repair materials tag: {} items", repairMaterials.size());
        tagsCreated++;

        // ===== WEAPON & TOOL TAGS (Fully Automated) =====
        var swordsBuilder = valueLookupBuilder(ItemTags.SWORDS);
        var swords = DatagenHelper.getSwords();
        swords.forEach(entry -> swordsBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Swords: {} items", swords.size());
        tagsCreated++;

        var pickaxesBuilder = valueLookupBuilder(ItemTags.PICKAXES);
        var pickaxes = DatagenHelper.getPickaxes();
        pickaxes.forEach(entry -> pickaxesBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Pickaxes: {} items", pickaxes.size());
        tagsCreated++;

        var shovelsBuilder = valueLookupBuilder(ItemTags.SHOVELS);
        var shovels = DatagenHelper.getShovels();
        shovels.forEach(entry -> shovelsBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Shovels: {} items", shovels.size());
        tagsCreated++;

        var axesBuilder = valueLookupBuilder(ItemTags.AXES);
        var axes = DatagenHelper.getAxes();
        axes.forEach(entry -> axesBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Axes: {} items", axes.size());
        tagsCreated++;

        var hoesBuilder = valueLookupBuilder(ItemTags.HOES);
        var hoes = DatagenHelper.getHoes();
        hoes.forEach(entry -> hoesBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Hoes: {} items", hoes.size());
        tagsCreated++;

        // ===== ARMOR TAGS (Fully Automated) =====
        var trimmableBuilder = valueLookupBuilder(ItemTags.TRIMMABLE_ARMOR);
        var trimmable = DatagenHelper.getTrimmableArmor();
        trimmable.forEach(entry -> trimmableBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Trimmable armor: {} items", trimmable.size());
        tagsCreated++;

        var headArmorBuilder = valueLookupBuilder(ItemTags.HEAD_ARMOR);
        var helmets = DatagenHelper.getHelmets();
        helmets.forEach(entry -> headArmorBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Head armor: {} items", helmets.size());
        tagsCreated++;

        var chestArmorBuilder = valueLookupBuilder(ItemTags.CHEST_ARMOR);
        var chestplates = DatagenHelper.getChestplates();
        chestplates.forEach(entry -> chestArmorBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Chest armor: {} items", chestplates.size());
        tagsCreated++;

        var legArmorBuilder = valueLookupBuilder(ItemTags.LEG_ARMOR);
        var leggings = DatagenHelper.getLeggings();
        leggings.forEach(entry -> legArmorBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Leg armor: {} items", leggings.size());
        tagsCreated++;

        var footArmorBuilder = valueLookupBuilder(ItemTags.FOOT_ARMOR);
        var boots = DatagenHelper.getBoots();
        boots.forEach(entry -> footArmorBuilder.add(entry.item()));
        PrimalCraft.LOGGER.info("  ✓ Foot armor: {} items", boots.size());
        tagsCreated++;

        // ===== TRIM MATERIALS =====
        var trimMaterials = DatagenHelper.getItemsContaining("pink_garnet").stream()
            .filter(entry -> entry.name().equals("pink_garnet"))
            .toList();
        trimMaterials.forEach(entry -> {
            valueLookupBuilder(ItemTags.TRIM_MATERIALS).add(entry.item());
        });
        PrimalCraft.LOGGER.info("  ✓ Trim materials: {} items", trimMaterials.size());
        tagsCreated++;

        // ===== LOGS & PLANKS (Automated from blocks) =====
        var logsBuilder = valueLookupBuilder(ItemTags.LOGS_THAT_BURN);
        var logs = DatagenHelper.getBlocksContaining("driftwood").stream()
            .filter(entry -> entry.name().contains("log") || entry.name().contains("wood"))
            .filter(entry -> !entry.name().contains("planks"))
            .toList();
        logs.forEach(entry -> logsBuilder.add(entry.asItem()));
        PrimalCraft.LOGGER.info("  ✓ Logs that burn: {} items", logs.size());
        tagsCreated++;

        var planksBuilder = valueLookupBuilder(ItemTags.PLANKS);
        var planks = DatagenHelper.getPlanks();
        planks.forEach(entry -> planksBuilder.add(entry.asItem()));
        PrimalCraft.LOGGER.info("  ✓ Planks: {} items", planks.size());
        tagsCreated++;

        long elapsed = System.currentTimeMillis() - startTime;
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  ITEM TAG GENERATION COMPLETE                              ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");
        PrimalCraft.LOGGER.info("  📊 Total Tags Created: {}", tagsCreated);
        PrimalCraft.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
    }
}
