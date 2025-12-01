package draylar.staffofbuilding.fabric.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.world.item.CreativeModeTabs;

public class ModItemGroups {
    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(ModItems.STAFF_BUILDING_WOOD);
            entries.accept(ModItems.STAFF_BUILDING_STONE);
            entries.accept(ModItems.STAFF_BUILDING_IRON);
            entries.accept(ModItems.STAFF_BUILDING_GOLDEN);
            entries.accept(ModItems.STAFF_BUILDING_DIAMOND);
            entries.accept(ModItems.STAFF_BUILDING_NETHERITE);
            entries.accept(ModItems.STAFF_BUILDING_INFINITE);
        });
    }
}
