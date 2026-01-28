package net.kimdog_studios.primal_craft.block.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.block.ModBlocks;
import net.kimdog_studios.primal_craft.block.entity.custom.GrowthChamberBlockEntity;
import net.kimdog_studios.primal_craft.block.entity.custom.PedestalBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<PedestalBlockEntity> PEDESTAL_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(PrimalCraft.MOD_ID, "pedestal_be"),
                    FabricBlockEntityTypeBuilder.create(PedestalBlockEntity::new, ModBlocks.PEDESTAL).build(null));

    public static final BlockEntityType<GrowthChamberBlockEntity> GROWTH_CHAMBER_BE =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(PrimalCraft.MOD_ID, "growth_chamber_be"),
                    FabricBlockEntityTypeBuilder.create(GrowthChamberBlockEntity::new, ModBlocks.GROWTH_CHAMBER).build(null));


    public static void registerBlockEntities() {
        PrimalCraft.LOGGER.info("Registering Block Entities for " + PrimalCraft.MOD_ID);
    }
}
