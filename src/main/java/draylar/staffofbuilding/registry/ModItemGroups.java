package draylar.staffofbuilding.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public class ModItemGroups {
    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
                    entries.add(ModItems.STAFF_BUILDING_WOOD);
                    entries.add(ModItems.STAFF_BUILDING_STONE);
                    entries.add(ModItems.STAFF_BUILDING_IRON);
                    entries.add(ModItems.STAFF_BUILDING_GOLDEN);
                    entries.add(ModItems.STAFF_BUILDING_DIAMOND);
                    entries.add(ModItems.STAFF_BUILDING_NETHERITE);
                    entries.add(ModItems.STAFF_BUILDING_INFINITE);
                }
        );
    }
}
