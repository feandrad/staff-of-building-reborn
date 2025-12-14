package draylar.staffofbuilding.fabric.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BlockUndoManager {

    private static final Map<UUID, UndoAction> UNDO_MAP = new HashMap<>();

    public static void record(ServerPlayer player, Level level, Map<BlockPos, BlockState> originalStates,
            net.minecraft.world.item.Item item, int count) {
        UNDO_MAP.put(player.getUUID(), new UndoAction(System.currentTimeMillis(), level, originalStates, item, count));
    }

    public static Optional<UndoAction> get(ServerPlayer player) {
        if (UNDO_MAP.containsKey(player.getUUID())) {
            UndoAction action = UNDO_MAP.get(player.getUUID());

            // 30 second timeout
            if (System.currentTimeMillis() - action.timestamp > 30000) {
                UNDO_MAP.remove(player.getUUID());
                return Optional.empty();
            }

            return Optional.of(action);
        }

        return Optional.empty();
    }

    public static void remove(ServerPlayer player) {
        UNDO_MAP.remove(player.getUUID());
    }

    public record UndoAction(long timestamp, Level level, Map<BlockPos, BlockState> originalStates,
            net.minecraft.world.item.Item item, int count) {
    }
}
