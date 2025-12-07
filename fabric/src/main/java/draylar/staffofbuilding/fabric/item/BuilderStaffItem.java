package draylar.staffofbuilding.fabric.item;

import draylar.staffofbuilding.fabric.StaffOfBuilding;
import draylar.staffofbuilding.fabric.api.SelectionCalculator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.List;

public class BuilderStaffItem extends Item {

    private final int size;
    private boolean invincible = false;

    public BuilderStaffItem(Properties properties, int size, ToolMaterial material) {
        super(properties.durability(material == null ? 0 : material.durability())
                .enchantable(material == null ? 100 : material.enchantmentValue())
                .repairable(material == null ? null : material.repairItems()));
        this.size = size;
    }

    public BuilderStaffItem invincible() {
        this.invincible = true;
        return this;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
            java.util.function.Consumer<Component> tooltip, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltip, tooltipFlag);
        tooltip.accept(Component.translatable("staffofbuilding.placement_range", size).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction side = context.getClickedFace();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        Block block = state.getBlock();
        Item item = block.asItem();

        // check to make sure the block we're placing off has an item
        if (player != null && item != Items.AIR && context.getHand() == InteractionHand.MAIN_HAND) {
            // get amount of required item in player inventory
            int count = 0;
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(item)) {
                    count += player.getInventory().getItem(i).getCount();
                }
            }
            if (player.getOffhandItem().is(item)) {
                count += player.getOffhandItem().getCount();
            }

            // run placement logic if they have at least 1 of the item (or if they are a
            // creative user)
            if (count > 0 || player.isCreative()) {
                // potentially reset state to prevent dupe or similar mechanics
                BlockState finalState = state;
                if (StaffOfBuilding.RESET_LIST.contains(state.getBlock()) || StaffOfBuilding.CLASS_RESET_LIST.stream()
                        .anyMatch(resetClass -> resetClass.isAssignableFrom(finalState.getBlock().getClass()))) {
                    state = state.getBlock().defaultBlockState();
                }

                // get number of blocks to place (min between max size and the count of items in
                // inventory)
                int maxChecks = Math.min(size, player.isCreative() ? size : count);
                List<BlockPos> positions = SelectionCalculator.calculateSelection(level, pos, side, maxChecks);
                int taken = 0;

                // do not play animation if we are not placing blocks
                if (positions.isEmpty()) {
                    return InteractionResult.FAIL;
                }

                if (!level.isClientSide()) {
                    // check if player has enough xp
                    int totalCost = positions.size();
                    if (!player.isCreative() && player.totalExperience < totalCost) {
                        return InteractionResult.FAIL;
                    }

                    // place blocks
                    for (BlockPos position : positions) {
                        BlockState originalState = level.getBlockState(position);
                        if (originalState
                                .canBeReplaced(new net.minecraft.world.item.context.BlockPlaceContext(context))) {
                            if (level.setBlock(position, state, 3)) {
                                taken++;
                                if (!player.isCreative()) {
                                    player.giveExperiencePoints(-1);
                                }
                            }
                        }
                    }

                    // take items from survival inventory
                    if (!player.isCreative()) {
                        int remaining = taken;
                        // remove items logic
                        // Simple removal logic since we don't have a helper easily available
                        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                            ItemStack s = player.getInventory().getItem(i);
                            if (s.is(item)) {
                                int toRemove = Math.min(remaining, s.getCount());
                                s.shrink(toRemove);
                                remaining -= toRemove;
                                if (remaining <= 0)
                                    break;
                            }
                        }
                        if (remaining > 0 && player.getOffhandItem().is(item)) {
                            player.getOffhandItem().shrink(remaining);
                        }
                    }

                    // damage item
                    if (context.getItemInHand().isDamageableItem() && !invincible) {
                        int damage = taken;

                        // Each damage tick has a [0% / 50% / 66% / 75%] to be ignored
                        for (int i = 0; i < damage; i++) {
                            if (level.random
                                    .nextInt(
                                            1 + EnchantmentHelper.getItemEnchantmentLevel(
                                                    level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                                                            .getOrThrow(Enchantments.UNBREAKING),
                                                    context.getItemInHand())) == 0) {
                                damage--;
                            }
                        }

                        if (player instanceof net.minecraft.server.level.ServerPlayer
                                && level instanceof net.minecraft.server.level.ServerLevel) {
                            context.getItemInHand().hurtAndBreak(Math.max(0, damage),
                                    (net.minecraft.server.level.ServerLevel) level,
                                    (net.minecraft.server.level.ServerPlayer) player, itemStack -> {
                                        // TODO: Find correct method for break event or play sound manually
                                        // player.broadcastBreakEvent(EquipmentSlot.MAINHAND);
                                    });
                        }
                    }

                    if (taken > 0) {
                        level.playSound(null, player.blockPosition(), state.getSoundType().getPlaceSound(),
                                SoundSource.PLAYERS, state.getSoundType().getVolume(), state.getSoundType().getPitch());
                    }

                    // TODO: Save positions and blocks to Player data to prepare undo command
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.FAIL;
    }

    public int getMaxSize() {
        return size;
    }
}
