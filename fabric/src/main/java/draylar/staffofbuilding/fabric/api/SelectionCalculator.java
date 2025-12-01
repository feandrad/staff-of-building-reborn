package draylar.staffofbuilding.fabric.api;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SelectionCalculator {

    public static List<BlockPos> calculateSelection(Level level, BlockPos originPos, Direction direction,
            int maxChecks) {
        BlockPos offsetPos = originPos.relative(direction);
        BlockState originState = level.getBlockState(originPos);

        // stored values, start checks at 1 for the origin position
        List<BlockPos> selectedPositions = new ArrayList<>();
        int checks = 1;

        // get start neighbors
        List<BlockPos> storedNeighbors = new ArrayList<>();
        if (level.isUnobstructed(originState, offsetPos, CollisionContext.empty())) {
            storedNeighbors.add(offsetPos);
        }

        while (checks < maxChecks && !storedNeighbors.isEmpty()) {
            // add new neighbors to stored list
            selectedPositions.addAll(storedNeighbors);
            List<BlockPos> newNeighbors = new ArrayList<>();

            // get new set of new neighbors from the current new neighbors
            for (BlockPos neighbor : storedNeighbors) {
                List<BlockPos> facingNeighbors = getValidNeighbors(level, neighbor, direction, originState);

                // add all facing neighbors that aren't already in the pool
                for (BlockPos facingNeighbor : facingNeighbors) {
                    if (checks < maxChecks) {
                        if (!selectedPositions.contains(facingNeighbor) && !storedNeighbors.contains(facingNeighbor)
                                && !newNeighbors.contains(facingNeighbor)) {
                            newNeighbors.add(facingNeighbor);
                            checks++;
                        }
                    }
                }
            }

            // clear new neighbors, set new ones
            storedNeighbors.clear();
            storedNeighbors.addAll(newNeighbors);
        }

        // add leftover stored neighbors
        selectedPositions.addAll(storedNeighbors);

        return selectedPositions.stream().filter(pos -> {
            return originState.canSurvive(level, pos);
        }).collect(Collectors.toList());
    }

    private static List<BlockPos> getValidNeighbors(Level level, BlockPos startPos, Direction facingDirection,
            BlockState originState) {
        List<BlockPos> foundNeighbors = new ArrayList<>();

        // check all side direction positions for the current facing direction to get
        // neighbors
        for (Vec3i checkDirection : getPotentialNeighbors(facingDirection)) {
            BlockPos offsetPos = startPos.offset(checkDirection);
            BlockState innerState = level.getBlockState(offsetPos.relative(facingDirection.getOpposite()));
            BlockState newState = level.getBlockState(offsetPos);

            // ensure inner state of neighbor position is the same as the original state the
            // player is looking at
            if (innerState.equals(originState) && (newState.isAir() || !newState.getFluidState().isEmpty())
                    && level.isUnobstructed(originState, offsetPos, CollisionContext.empty())) {
                foundNeighbors.add(offsetPos);
            }
        }

        return foundNeighbors;
    }

    // todo: cache these?
    // grabs a list of facing neighbors
    private static List<Vec3i> getPotentialNeighbors(Direction direction) {
        ArrayList<Vec3i> directions = new ArrayList<>();

        if (direction.getAxis() == Direction.Axis.Y) {
            directions
                    .add(new Vec3i(Direction.NORTH.getStepX(), Direction.NORTH.getStepY(), Direction.NORTH.getStepZ()));
            directions.add(new Vec3i(Direction.EAST.getStepX(), Direction.EAST.getStepY(), Direction.EAST.getStepZ()));
            directions
                    .add(new Vec3i(Direction.SOUTH.getStepX(), Direction.SOUTH.getStepY(), Direction.SOUTH.getStepZ()));
            directions.add(new Vec3i(Direction.WEST.getStepX(), Direction.WEST.getStepY(), Direction.WEST.getStepZ()));

            // Diagonals
            directions.add(new Vec3i(-1, 0, -1)); // West + North
            directions.add(new Vec3i(1, 0, -1)); // East + North
            directions.add(new Vec3i(1, 0, 1)); // East + South
            directions.add(new Vec3i(-1, 0, 1)); // West + South
        } else {
            Direction clockwise = direction.getClockWise();
            Direction counterClockwise = direction.getCounterClockWise();
            directions.add(new Vec3i(clockwise.getStepX(), clockwise.getStepY(), clockwise.getStepZ()));
            directions.add(
                    new Vec3i(counterClockwise.getStepX(), counterClockwise.getStepY(), counterClockwise.getStepZ()));
            directions.add(new Vec3i(Direction.UP.getStepX(), Direction.UP.getStepY(), Direction.UP.getStepZ()));
            directions.add(new Vec3i(Direction.DOWN.getStepX(), Direction.DOWN.getStepY(), Direction.DOWN.getStepZ()));
        }

        return directions;
    }
}
