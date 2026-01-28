package net.kimdog_studios.primal_craft.item;

import net.kimdog_studios.primal_craft.util.ModTags;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;

public class ModToolMaterials {
    // Existing custom material
    public static ToolMaterial PINK_GARNET = new ToolMaterial(ModTags.Blocks.INCORRECT_FOR_PINK_GARNET_TOOL,
            1200, 5.0F, 4.0F, 22, ModTags.Items.PINK_GARNET_REPAIR);

    public static ToolMaterial OBSIDIAN = new ToolMaterial(ModTags.Blocks.INCORRECT_FOR_OBSIDIAN_TOOL,
            2000, 8.0F, 3.0F, 15, ModTags.Items.PINK_GARNET_REPAIR);
}
