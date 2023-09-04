package draylar.staffofbuilding;

import draylar.staffofbuilding.registry.ModItemGroups;
import draylar.staffofbuilding.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.List;

public class StaffOfBuilding implements ModInitializer {

    public static final String MOD_ID = "staffofbuilding";
    public static final List<Block> RESET_LIST = Arrays.asList(Blocks.SNOW, Blocks.COMPOSTER, Blocks.CAULDRON, Blocks.CAKE, Blocks.BEEHIVE, Blocks.BREWING_STAND, Blocks.TURTLE_EGG, Blocks.SEA_PICKLE);
    public static final List<Class<? extends Block>> CLASS_RESET_LIST = Arrays.asList(CropBlock.class, AbstractCandleBlock.class);

    @Override
    public void onInitialize() {
        ModItems.init();
        ModItemGroups.init();
    }

    public static Identifier id(String name) {
        return new Identifier(MOD_ID, name);
    }
}
