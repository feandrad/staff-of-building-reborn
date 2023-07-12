package draylar.staffofbuilding.registry;

import draylar.staffofbuilding.StaffOfBuilding;
import draylar.staffofbuilding.config.StaffOfBuildingConfig;
import draylar.staffofbuilding.item.BuilderStaffItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.registry.Registry;

public class ModItems {

    static StaffOfBuildingConfig CONFIG = StaffOfBuildingConfig.load();

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registry.ITEM, StaffOfBuilding.id(name), item);
    }

    public static void init() {
        if (CONFIG.woodenEnabled) register("wooden_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.woodenSize, ToolMaterials.WOOD));
        if (CONFIG.stoneEnabled) register("stone_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.stoneSize, ToolMaterials.STONE)); // +8
        if (CONFIG.ironEnabled) register("iron_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.ironSize, ToolMaterials.IRON)); // +12
        if (CONFIG.goldenEnabled) register("golden_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.goldenSize, ToolMaterials.WOOD)); // +16
        if (CONFIG.diamondEnabled) register("diamond_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.diamondSize, ToolMaterials.DIAMOND)); // +16
        if (CONFIG.netheriteEnabled) register("netherite_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS).fireproof(), CONFIG.netheriteSize, ToolMaterials.NETHERITE)); // +20
        if (CONFIG.infiniteEnabled) register("infinite_builder_staff", new BuilderStaffItem(new Item.Settings().group(ItemGroup.TOOLS), CONFIG.infiniteSize, null).invincible()); // + 24 + 28 + 32
    }

    private ModItems() {
        // NO-OP
    }
}
