package net.kimdog_studios.primal_craft.world.gen;

public class ModWorldGeneration {
    public static void generateModWorldGen() {
        ModOreGeneration.generateOres();

        ModTreeGeneration.generateTrees();
        ModBushGeneration.generateBushes();

        ModEntitySpawns.addSpawns();
    }
}
