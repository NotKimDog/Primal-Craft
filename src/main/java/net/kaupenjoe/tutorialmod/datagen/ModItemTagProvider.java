package net.kaupenjoe.tutorialmod.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.kaupenjoe.tutorialmod.TutorialMod;
import net.kaupenjoe.tutorialmod.util.ModTags;
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
        TutorialMod.LOGGER.info("Starting automated item tag generation...");

        // ===== TRANSFORMABLE ITEMS (Custom tag) =====
        var transformableBuilder = valueLookupBuilder(ModTags.Items.TRANSFORMABLE_ITEMS);

        // Auto-add all items containing these keywords
        DatagenHelper.getItemsContaining("pink_garnet", "stick").forEach(entry -> {
            transformableBuilder.add(entry.item());
        });

        // Add vanilla items
        transformableBuilder.add(Items.COAL, Items.APPLE);

        TutorialMod.LOGGER.info("  ✓ Transformable items tag");

        // ===== REPAIR MATERIALS =====
        DatagenHelper.getItemsContaining("pink_garnet").stream()
            .filter(entry -> entry.name().equals("pink_garnet"))
            .forEach(entry -> {
                valueLookupBuilder(ModTags.Items.PINK_GARNET_REPAIR).add(entry.item());
            });

        TutorialMod.LOGGER.info("  ✓ Repair materials tag");

        // ===== WEAPON & TOOL TAGS (Fully Automated) =====
        var swordsBuilder = valueLookupBuilder(ItemTags.SWORDS);
        DatagenHelper.getSwords().forEach(entry -> swordsBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Swords: {} items", DatagenHelper.getSwords().size());

        var pickaxesBuilder = valueLookupBuilder(ItemTags.PICKAXES);
        DatagenHelper.getPickaxes().forEach(entry -> pickaxesBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Pickaxes: {} items", DatagenHelper.getPickaxes().size());

        var shovelsBuilder = valueLookupBuilder(ItemTags.SHOVELS);
        DatagenHelper.getShovels().forEach(entry -> shovelsBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Shovels: {} items", DatagenHelper.getShovels().size());

        var axesBuilder = valueLookupBuilder(ItemTags.AXES);
        DatagenHelper.getAxes().forEach(entry -> axesBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Axes: {} items", DatagenHelper.getAxes().size());

        var hoesBuilder = valueLookupBuilder(ItemTags.HOES);
        DatagenHelper.getHoes().forEach(entry -> hoesBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Hoes: {} items", DatagenHelper.getHoes().size());

        // ===== ARMOR TAGS (Fully Automated) =====
        var trimmableBuilder = valueLookupBuilder(ItemTags.TRIMMABLE_ARMOR);
        DatagenHelper.getTrimmableArmor().forEach(entry -> trimmableBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Trimmable armor: {} items", DatagenHelper.getTrimmableArmor().size());

        var headArmorBuilder = valueLookupBuilder(ItemTags.HEAD_ARMOR);
        DatagenHelper.getHelmets().forEach(entry -> headArmorBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Head armor: {} items", DatagenHelper.getHelmets().size());

        var chestArmorBuilder = valueLookupBuilder(ItemTags.CHEST_ARMOR);
        DatagenHelper.getChestplates().forEach(entry -> chestArmorBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Chest armor: {} items", DatagenHelper.getChestplates().size());

        var legArmorBuilder = valueLookupBuilder(ItemTags.LEG_ARMOR);
        DatagenHelper.getLeggings().forEach(entry -> legArmorBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Leg armor: {} items", DatagenHelper.getLeggings().size());

        var footArmorBuilder = valueLookupBuilder(ItemTags.FOOT_ARMOR);
        DatagenHelper.getBoots().forEach(entry -> footArmorBuilder.add(entry.item()));
        TutorialMod.LOGGER.info("  ✓ Foot armor: {} items", DatagenHelper.getBoots().size());

        // ===== TRIM MATERIALS =====
        DatagenHelper.getItemsContaining("pink_garnet").stream()
            .filter(entry -> entry.name().equals("pink_garnet"))
            .forEach(entry -> {
                valueLookupBuilder(ItemTags.TRIM_MATERIALS).add(entry.item());
            });

        // ===== LOGS & PLANKS (Automated from blocks) =====
        var logsBuilder = valueLookupBuilder(ItemTags.LOGS_THAT_BURN);
        DatagenHelper.getBlocksContaining("driftwood").stream()
            .filter(entry -> entry.name().contains("log") || entry.name().contains("wood"))
            .filter(entry -> !entry.name().contains("planks"))
            .forEach(entry -> logsBuilder.add(entry.asItem()));
        TutorialMod.LOGGER.info("  ✓ Logs that burn");

        var planksBuilder = valueLookupBuilder(ItemTags.PLANKS);
        DatagenHelper.getPlanks().forEach(entry -> planksBuilder.add(entry.asItem()));
        TutorialMod.LOGGER.info("  ✓ Planks");

        long elapsed = System.currentTimeMillis() - startTime;
        TutorialMod.LOGGER.info("Automated item tag generation completed in {}ms", elapsed);
    }
}
