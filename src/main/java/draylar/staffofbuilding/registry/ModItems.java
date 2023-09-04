package draylar.staffofbuilding.registry;

import draylar.staffofbuilding.StaffOfBuilding;
import draylar.staffofbuilding.config.StaffOfBuildingConfig;
import draylar.staffofbuilding.item.BuilderStaffItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModItems {

    static StaffOfBuildingConfig CONFIG = StaffOfBuildingConfig.load();

    final public static BuilderStaffItem STAFF_BUILDING_WOOD = new BuilderStaffItem(new Item.Settings(), CONFIG.woodenSize, ToolMaterials.WOOD);
    final public static BuilderStaffItem STAFF_BUILDING_STONE = new BuilderStaffItem(new Item.Settings(), CONFIG.stoneSize, ToolMaterials.STONE);
    final public static BuilderStaffItem STAFF_BUILDING_IRON = new BuilderStaffItem(new Item.Settings(), CONFIG.ironSize, ToolMaterials.IRON);
    final public static BuilderStaffItem STAFF_BUILDING_GOLDEN = new BuilderStaffItem(new Item.Settings(), CONFIG.goldenSize, ToolMaterials.WOOD);
    final public static BuilderStaffItem STAFF_BUILDING_DIAMOND = new BuilderStaffItem(new Item.Settings(), CONFIG.diamondSize, ToolMaterials.DIAMOND);
    final public static BuilderStaffItem STAFF_BUILDING_NETHERITE = new BuilderStaffItem(new Item.Settings().fireproof(), CONFIG.netheriteSize, ToolMaterials.NETHERITE);
    final public static BuilderStaffItem STAFF_BUILDING_INFINITE = new BuilderStaffItem(new Item.Settings(), CONFIG.infiniteSize, null).invincible();

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, StaffOfBuilding.id(name), item);
    }

    public static void init() {
        if (CONFIG.woodenEnabled) register("wooden_builder_staff", STAFF_BUILDING_WOOD);
        if (CONFIG.stoneEnabled) register("stone_builder_staff", STAFF_BUILDING_STONE);
        if (CONFIG.ironEnabled) register("iron_builder_staff", STAFF_BUILDING_IRON);
        if (CONFIG.goldenEnabled) register("golden_builder_staff", STAFF_BUILDING_GOLDEN);
        if (CONFIG.diamondEnabled) register("diamond_builder_staff", STAFF_BUILDING_DIAMOND);
        if (CONFIG.netheriteEnabled) register("netherite_builder_staff", STAFF_BUILDING_NETHERITE);
        if (CONFIG.infiniteEnabled) register("infinite_builder_staff", STAFF_BUILDING_INFINITE);
    }

    private ModItems() {
        // NO-OP
    }
}
