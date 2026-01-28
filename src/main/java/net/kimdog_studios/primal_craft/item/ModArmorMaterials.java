package net.kimdog_studios.primal_craft.item;

import net.kimdog_studios.primal_craft.PrimalCraft;
import net.kimdog_studios.primal_craft.util.ModTags;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.EquipmentAsset;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.EnumMap;

public class ModArmorMaterials {
    static RegistryKey<? extends Registry<EquipmentAsset>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.ofVanilla("equipment_asset"));
    public static final RegistryKey<EquipmentAsset> PINK_GARNET_KEY = RegistryKey.of(REGISTRY_KEY, Identifier.of(PrimalCraft.MOD_ID, "pink_garnet"));
    public static final RegistryKey<EquipmentAsset> PAJAMA_KEY = RegistryKey.of(REGISTRY_KEY, Identifier.of(PrimalCraft.MOD_ID, "pajama"));

    public static final ArmorMaterial PINK_GARNET_ARMOR_MATERIAL = new ArmorMaterial(500, Util.make(new EnumMap<>(EquipmentType.class), map -> {
        map.put(EquipmentType.BOOTS, 2);
        map.put(EquipmentType.LEGGINGS, 4);
        map.put(EquipmentType.CHESTPLATE, 6);
        map.put(EquipmentType.HELMET, 2);
        map.put(EquipmentType.BODY, 4);
    }), 20, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,0,0, ModTags.Items.PINK_GARNET_REPAIR, PINK_GARNET_KEY);

    public static final ArmorMaterial PAJAMA_ARMOR_MATERIAL = new ArmorMaterial(50, Util.make(new EnumMap<>(EquipmentType.class), map -> {
        map.put(EquipmentType.BOOTS, 0);
        map.put(EquipmentType.LEGGINGS, 1);
        map.put(EquipmentType.CHESTPLATE, 1);
        map.put(EquipmentType.HELMET, 0);
        map.put(EquipmentType.BODY, 1);
    }), 5, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,0,0, ModTags.Items.PINK_GARNET_REPAIR, PAJAMA_KEY);
}
