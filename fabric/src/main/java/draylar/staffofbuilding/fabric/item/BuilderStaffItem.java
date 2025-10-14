package draylar.staffofbuilding.fabric.item;

import draylar.staffofbuilding.fabric.StaffOfBuilding;
import draylar.staffofbuilding.fabric.api.SelectionCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.List;

public class BuilderStaffItem extends Item {

    private final int size;
    private final ToolMaterial material;
    private boolean invincible = false;

    public BuilderStaffItem(Settings settings, int size, ToolMaterial material) {
        super(settings.maxDamage(material == null ? 0 : material.getDurability()));
        this.size = size;
        this.material = material;
    }

    public BuilderStaffItem invincible() {
        this.invincible = true;
        return this;
    }

    @Override
    public boolean isDamageable() {
        return !invincible && super.isDamageable();
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return material != null && material.getRepairIngredient().test(ingredient);
    }

    @Override
    public int getEnchantability() {
        return material == null ? 100 : material.getEnchantability();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(Text.translatable("staffofbuilding.placement_range", size).formatted(Formatting.GRAY));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Direction side = context.getSide();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        Block block = state.getBlock();
        Item item = block.asItem();

        // check to make sure the block we're placing off has an item
        if (player != null && item != Items.AIR && context.getHand() == Hand.MAIN_HAND) {
            // get amount of required item in player inventory
            int count = player.getInventory().count(item);

            // run placement logic if they have at least 1 of the item (or if they are a creative user)
            if (count > 0 || player.isCreative()) {
                // potentially reset state to prevent dupe or similar  mechanics
                BlockState finalState = state;
                if (StaffOfBuilding.RESET_LIST.contains(state.getBlock()) || StaffOfBuilding.CLASS_RESET_LIST.stream().anyMatch(resetClass -> resetClass.isAssignableFrom(finalState.getBlock().getClass()))) {
                    state = state.getBlock().getDefaultState();
                }

                // get number of blocks to place (min between max size and the count of items in inventory)
                int maxChecks = Math.min(size, player.isCreative() ? size : count);
                List<BlockPos> positions = SelectionCalculator.calculateSelection(world, pos, side, maxChecks);
                int taken = 0;

                // do not play animation if we are not placing blocks
                if (positions.isEmpty()) {
                    return ActionResult.FAIL;
                }

                if (!world.isClient) {
                    // place blocks
                    for (BlockPos position : positions) {
                        BlockState originalState = world.getBlockState(position);
                        if (originalState.isAir() || !originalState.getFluidState().isEmpty()) {
                            world.setBlockState(position, state);
                            taken++;
                        }
                    }

                    // take items from survival inventory
                    if (!player.isCreative()) {
                        player.getInventory().remove(stack -> stack.getItem().equals(item), taken, player.getInventory());
                    }

                    // damage item
                    if (context.getStack().isDamageable()) {
                        int damage = taken;

                        // Each damage tick has a [0% / 50% / 66% / 75%] to be ignored
                        for (int i = 0; i < damage; i++) {
                            if (world.random.nextInt(1 + EnchantmentHelper.getLevel(Enchantments.UNBREAKING, context.getStack())) == 0) {
                                damage--;
                            }
                        }

                        context.getStack().damage(Math.max(0, damage), player, (livingEntity) -> {
                            livingEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
                        });
                    }

                    if (taken > 0) {
                        world.playSound(null, player.getBlockPos(), state.getSoundGroup().getPlaceSound(), SoundCategory.PLAYERS, state.getSoundGroup().getVolume(), state.getSoundGroup().getPitch());
                    }

                    // TODO: Save positions and blocks to Player data to prepare undo command
                }

                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.FAIL;
    }

    public int getMaxSize() {
        return size;
    }
}
