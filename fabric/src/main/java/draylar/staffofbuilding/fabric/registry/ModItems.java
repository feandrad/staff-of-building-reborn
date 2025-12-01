package draylar.staffofbuilding.fabric.registry;

import draylar.staffofbuilding.fabric.StaffOfBuilding;
import draylar.staffofbuilding.fabric.config.StaffOfBuildingConfig;
import draylar.staffofbuilding.fabric.item.BuilderStaffItem;
import net.minecraft.core.Registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public class ModItems {

    static StaffOfBuildingConfig CONFIG = StaffOfBuildingConfig.load();

    private static Item.Properties itemProperties(String id) {
        net.minecraft.resources.ResourceLocation loc = StaffOfBuilding.id(id);

        // Manually create registry key to avoid potential null Registries.ITEM during
        // early init
        net.minecraft.resources.ResourceKey<net.minecraft.core.Registry<Item>> registryKey = net.minecraft.resources.ResourceKey
                .createRegistryKey(net.minecraft.resources.ResourceLocation.withDefaultNamespace("item"));

        net.minecraft.resources.ResourceKey<Item> key = net.minecraft.resources.ResourceKey.create(registryKey, loc);

        return new Item.Properties().setId(key);
    }

    final public static BuilderStaffItem STAFF_BUILDING_WOOD = new BuilderStaffItem(
            itemProperties("wooden_builder_staff"),
            CONFIG.woodenSize, ToolMaterial.WOOD);
    final public static BuilderStaffItem STAFF_BUILDING_STONE = new BuilderStaffItem(
            itemProperties("stone_builder_staff"),
            CONFIG.stoneSize, ToolMaterial.STONE);
    final public static BuilderStaffItem STAFF_BUILDING_IRON = new BuilderStaffItem(
            itemProperties("iron_builder_staff"),
            CONFIG.ironSize, ToolMaterial.IRON);
    final public static BuilderStaffItem STAFF_BUILDING_GOLDEN = new BuilderStaffItem(
            itemProperties("golden_builder_staff"),
            CONFIG.goldenSize, ToolMaterial.WOOD);
    final public static BuilderStaffItem STAFF_BUILDING_DIAMOND = new BuilderStaffItem(
            itemProperties("diamond_builder_staff"),
            CONFIG.diamondSize, ToolMaterial.DIAMOND);
    final public static BuilderStaffItem STAFF_BUILDING_NETHERITE = new BuilderStaffItem(
            itemProperties("netherite_builder_staff").fireResistant(), CONFIG.netheriteSize, ToolMaterial.NETHERITE);
    final public static BuilderStaffItem STAFF_BUILDING_INFINITE = new BuilderStaffItem(
            itemProperties("infinite_builder_staff"),
            CONFIG.infiniteSize, null).invincible();

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, StaffOfBuilding.id(name), item);
    }

    public static void init() {
        if (CONFIG.woodenEnabled)
            register("wooden_builder_staff", STAFF_BUILDING_WOOD);
        if (CONFIG.stoneEnabled)
            register("stone_builder_staff", STAFF_BUILDING_STONE);
        if (CONFIG.ironEnabled)
            register("iron_builder_staff", STAFF_BUILDING_IRON);
        if (CONFIG.goldenEnabled)
            register("golden_builder_staff", STAFF_BUILDING_GOLDEN);
        if (CONFIG.diamondEnabled)
            register("diamond_builder_staff", STAFF_BUILDING_DIAMOND);
        if (CONFIG.netheriteEnabled)
            register("netherite_builder_staff", STAFF_BUILDING_NETHERITE);
        if (CONFIG.infiniteEnabled)
            register("infinite_builder_staff", STAFF_BUILDING_INFINITE);
    }

    private ModItems() {
        // NO-OP
    }
}
