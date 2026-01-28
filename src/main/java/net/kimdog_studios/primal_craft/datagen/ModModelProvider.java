package net.kimdog_studios.primal_craft.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.block.custom.CauliflowerCropBlock;
import net.kimdog_studios.primal_craft.block.custom.HoneyBerryBushBlock;
import net.kimdog_studios.primal_craft.block.custom.PinkGarnetLampBlock;
import net.kimdog_studios.primal_craft.component.ModDataComponentTypes;
import net.kimdog_studios.primal_craft.item.ModArmorMaterials;
import net.kimdog_studios.primal_craft.item.ModItems;
import net.minecraft.client.data.*;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.client.render.item.model.ConditionItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.property.bool.HasComponentProperty;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.client.render.model.json.WeightedVariant;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;

import java.util.Optional;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        long startTime = System.currentTimeMillis();
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  STARTING BLOCKSTATE & MODEL GENERATION                    ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        int blocksGenerated = 0;

        BlockStateModelGenerator.BlockTexturePool pinkGarnetPool =
                blockStateModelGenerator.registerCubeAllModelTexturePool(ModBlocks.PINK_GARNET_BLOCK);

        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.RAW_PINK_GARNET_BLOCK);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.PINK_GARNET_ORE);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.PINK_GARNET_DEEPSLATE_ORE);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.PINK_GARNET_NETHER_ORE);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.PINK_GARNET_END_ORE);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.MAGIC_BLOCK);
        blocksGenerated++;

        PrimalCraft.LOGGER.debug("  ✓ Generated {} ore/base block models", 6);

        pinkGarnetPool.stairs(ModBlocks.PINK_GARNET_STAIRS);
        blocksGenerated++;
        pinkGarnetPool.slab(ModBlocks.PINK_GARNET_SLAB);
        blocksGenerated++;
        pinkGarnetPool.button(ModBlocks.PINK_GARNET_BUTTON);
        blocksGenerated++;
        pinkGarnetPool.pressurePlate(ModBlocks.PINK_GARNET_PRESSURE_PLATE);
        blocksGenerated++;
        pinkGarnetPool.fence(ModBlocks.PINK_GARNET_FENCE);
        blocksGenerated++;
        pinkGarnetPool.fenceGate(ModBlocks.PINK_GARNET_FENCE_GATE);
        blocksGenerated++;
        pinkGarnetPool.wall(ModBlocks.PINK_GARNET_WALL);
        blocksGenerated++;

        PrimalCraft.LOGGER.debug("  ✓ Generated {} variant models (stairs, slab, etc.)", 7);

        blockStateModelGenerator.registerDoor(ModBlocks.PINK_GARNET_DOOR);
        blocksGenerated++;
        blockStateModelGenerator.registerTrapdoor(ModBlocks.PINK_GARNET_TRAPDOOR);
        blocksGenerated++;

        PrimalCraft.LOGGER.debug("  ✓ Generated door & trapdoor models");

        Identifier lampOffIdentifier =
                TexturedModel.CUBE_ALL.upload(ModBlocks.PINK_GARNET_LAMP, blockStateModelGenerator.modelCollector);
        Identifier lampOnIdentifier =
                blockStateModelGenerator.createSubModel(ModBlocks.PINK_GARNET_LAMP, "_on", Models.CUBE_ALL, TextureMap::all);

        blockStateModelGenerator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(ModBlocks.PINK_GARNET_LAMP)
                        .with(BlockStateModelGenerator.createBooleanModelMap(
                                PinkGarnetLampBlock.CLICKED,
                                new WeightedVariant(Pool.<ModelVariant>builder()
                                        .add(new ModelVariant(lampOnIdentifier)).build()),
                                new WeightedVariant(Pool.<ModelVariant>builder()
                                        .add(new ModelVariant(lampOffIdentifier)).build())
                        ))
        );
        blocksGenerated++;
        PrimalCraft.LOGGER.debug("  ✓ Generated lamp variants (on/off)");

        blockStateModelGenerator.registerCrop(
                ModBlocks.CAULIFLOWER_CROP,
                CauliflowerCropBlock.AGE,
                0, 1, 2, 3, 4, 5, 6
        );
        blocksGenerated++;
        PrimalCraft.LOGGER.debug("  ✓ Generated crop model (7 stages)");

        blockStateModelGenerator.registerTintableCrossBlockStateWithStages(
                ModBlocks.HONEY_BERRY_BUSH,
                BlockStateModelGenerator.CrossType.NOT_TINTED,
                HoneyBerryBushBlock.AGE,
                0, 1, 2, 3
        );
        blocksGenerated++;
        PrimalCraft.LOGGER.debug("  ✓ Generated berry bush model (4 stages)");

        blockStateModelGenerator.createLogTexturePool(ModBlocks.DRIFTWOOD_LOG)
                .log(ModBlocks.DRIFTWOOD_LOG)
                .wood(ModBlocks.DRIFTWOOD_WOOD);
        blocksGenerated += 2;
        blockStateModelGenerator.createLogTexturePool(ModBlocks.STRIPPED_DRIFTWOOD_LOG)
                .log(ModBlocks.STRIPPED_DRIFTWOOD_LOG)
                .wood(ModBlocks.STRIPPED_DRIFTWOOD_WOOD);
        blocksGenerated += 2;
        PrimalCraft.LOGGER.debug("  ✓ Generated log & wood models (4 variants)");

        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.DRIFTWOOD_PLANKS);
        blocksGenerated++;
        blockStateModelGenerator.registerSingleton(ModBlocks.DRIFTWOOD_LEAVES, TexturedModel.LEAVES);
        blocksGenerated++;
        blockStateModelGenerator.registerTintableCrossBlockState(
                ModBlocks.DRIFTWOOD_SAPLING,
                BlockStateModelGenerator.CrossType.NOT_TINTED
        );
        blocksGenerated++;
        PrimalCraft.LOGGER.debug("  ✓ Generated wood block models (planks, leaves, sapling)");

        blockStateModelGenerator.registerNorthDefaultHorizontalRotatable(ModBlocks.CHAIR);
        blocksGenerated++;
        blockStateModelGenerator.registerSimpleCubeAll(ModBlocks.GROWTH_CHAMBER);
        blocksGenerated++;
        PrimalCraft.LOGGER.debug("  ✓ Generated furniture models");

        long elapsed = System.currentTimeMillis() - startTime;
        PrimalCraft.LOGGER.info("  📊 Block Models Generated: {}", blocksGenerated);
        PrimalCraft.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        long startTime = System.currentTimeMillis();
        PrimalCraft.LOGGER.info("╔════════════════════════════════════════════════════════════╗");
        PrimalCraft.LOGGER.info("║  STARTING ITEM MODEL GENERATION                            ║");
        PrimalCraft.LOGGER.info("╚════════════════════════════════════════════════════════════╝");

        int itemsGenerated = 0;

        itemModelGenerator.register(ModItems.PINK_GARNET, Models.GENERATED);
        itemModelGenerator.register(ModItems.RAW_PINK_GARNET, Models.GENERATED);
        itemsGenerated += 2;
        PrimalCraft.LOGGER.debug("  • Generated material item models");

        itemModelGenerator.register(ModItems.CAULIFLOWER, Models.GENERATED);
        itemModelGenerator.register(ModItems.STARLIGHT_ASHES, Models.GENERATED);
        itemsGenerated += 2;
        PrimalCraft.LOGGER.debug("  • Generated crop item models");

        // Sticks (various wood types) - use simple generated models
        itemModelGenerator.register(ModItems.ACACIA_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.BIRCH_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.CHERRY_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.CRIMSON_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.DARK_OAK_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.JUNGLE_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.MANGROVE_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.PALE_OAK_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.SPRUCE_STICK, Models.GENERATED);
        itemModelGenerator.register(ModItems.WARPED_STICK, Models.GENERATED);
        itemsGenerated += 10;
        PrimalCraft.LOGGER.debug("  • Generated 10 stick variants");

        itemModelGenerator.register(ModItems.PINK_GARNET_SWORD, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PINK_GARNET_PICKAXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PINK_GARNET_SHOVEL, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PINK_GARNET_AXE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PINK_GARNET_HOE, Models.HANDHELD);
        itemModelGenerator.register(ModItems.PINK_GARNET_HAMMER, Models.HANDHELD);
        itemsGenerated += 6;

        itemModelGenerator.register(ModItems.OBSIDIAN_AXE, Models.HANDHELD);
        itemsGenerated++;
        PrimalCraft.LOGGER.debug("  • Generated 7 tool models");

        itemModelGenerator.upload(ModItems.KAUPEN_BOW, Models.BOW);
        itemModelGenerator.registerBow(ModItems.KAUPEN_BOW);
        itemsGenerated++;
        PrimalCraft.LOGGER.debug("  • Generated bow model");

        itemModelGenerator.registerArmor(ModItems.PINK_GARNET_HELMET, ModArmorMaterials.PINK_GARNET_KEY,
                ItemModelGenerator.HELMET_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.PINK_GARNET_CHESTPLATE, ModArmorMaterials.PINK_GARNET_KEY,
                ItemModelGenerator.CHESTPLATE_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.PINK_GARNET_LEGGINGS, ModArmorMaterials.PINK_GARNET_KEY,
                ItemModelGenerator.LEGGINGS_TRIM_ID_PREFIX, false);
        itemModelGenerator.registerArmor(ModItems.PINK_GARNET_BOOTS, ModArmorMaterials.PINK_GARNET_KEY,
                ItemModelGenerator.BOOTS_TRIM_ID_PREFIX, false);
        itemsGenerated += 4;
        PrimalCraft.LOGGER.debug("  • Generated 4 armor piece models with trim support");

        itemModelGenerator.register(ModItems.PINK_GARNET_HORSE_ARMOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.KAUPEN_SMITHING_TEMPLATE, Models.GENERATED);
        itemModelGenerator.register(ModItems.BAR_BRAWL_MUSIC_DISC, Models.GENERATED);
        itemModelGenerator.register(ModBlocks.DRIFTWOOD_SAPLING.asItem(), Models.GENERATED);
        itemsGenerated += 4;
        PrimalCraft.LOGGER.debug("  • Generated decorative/special item models");

        itemModelGenerator.register(ModItems.MANTIS_SPAWN_EGG,
                new Model(Optional.of(Identifier.of("item/template_spawn_egg")), Optional.empty()));
        itemsGenerated++;
        PrimalCraft.LOGGER.debug("  • Generated spawn egg model");

        ItemModel.Unbaked unbakedChisel =
                ItemModels.basic(itemModelGenerator.upload(ModItems.CHISEL, Models.GENERATED));
        ItemModel.Unbaked unbakedUsedChisel =
                ItemModels.basic(itemModelGenerator.registerSubModel(ModItems.CHISEL, "_used", Models.GENERATED));

        itemModelGenerator.output.accept(ModItems.CHISEL,
                new ItemAsset(
                        new ConditionItemModel.Unbaked(
                                new HasComponentProperty(ModDataComponentTypes.COORDINATES, false),
                                unbakedUsedChisel,
                                unbakedChisel
                        ),
                        new ItemAsset.Properties(false, false, 1f)
                ).model()
        );
        itemsGenerated++;
        PrimalCraft.LOGGER.debug("  • Generated conditional item models (chisel variants)");

        long elapsed = System.currentTimeMillis() - startTime;
        PrimalCraft.LOGGER.info("  📊 Item Models Generated: {}", itemsGenerated);
        PrimalCraft.LOGGER.info("  ⏱️  Execution Time: {}ms", elapsed);
    }
}