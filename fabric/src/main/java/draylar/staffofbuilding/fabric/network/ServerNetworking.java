package draylar.staffofbuilding.fabric.network;

import draylar.staffofbuilding.fabric.item.BuilderStaffItem;
import draylar.staffofbuilding.fabric.util.BlockUndoManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;

public class ServerNetworking {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(UndoMessage.ID, UndoMessage.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(UndoMessage.ID, (payload, context) -> {
            context.server().execute(() -> handleUndo(context.player()));
        });
    }

    private static void handleUndo(ServerPlayer player) {
        // Player must be holding a Builder Staff
        if (!(player.getMainHandItem().getItem() instanceof BuilderStaffItem)) {
            return;
        }

        Optional<BlockUndoManager.UndoAction> actionOptional = BlockUndoManager.get(player);

        if (actionOptional.isPresent()) {
            BlockUndoManager.UndoAction action = actionOptional.get();
            Map<BlockPos, BlockState> history = action.originalStates();

            // Restore blocks
            history.forEach((pos, state) -> {
                action.level().setBlock(pos, state, 3);
            });

            // Refund items
            if (!player.isCreative() && action.count() > 0) {
                net.minecraft.world.item.ItemStack refundStack = new net.minecraft.world.item.ItemStack(action.item(),
                        action.count());
                if (!player.getInventory().add(refundStack)) {
                    player.drop(refundStack, false);
                }
            }

            // Play sound? Particles?
            // For now just message
            player.displayClientMessage(Component.translatable("staffofbuilding.undo_complete"), true);

            // Remove from history
            BlockUndoManager.remove(player);
        } else {
            player.displayClientMessage(Component.translatable("staffofbuilding.undo_failed_timeout"), true);
        }
    }
}
