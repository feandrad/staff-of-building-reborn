package draylar.staffofbuilding.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import draylar.staffofbuilding.fabric.api.SelectionCalculator;
import draylar.staffofbuilding.fabric.item.BuilderStaffItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ClientLevel level;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;

    @Inject(method = "renderHitOutline", at = @At("HEAD"), cancellable = true)
    private void renderWandHighlight(PoseStack poseStack, VertexConsumer vertexConsumer, double d,
            double e, double f, net.minecraft.client.renderer.state.BlockOutlineRenderState blockOutlineRenderState,
            int color, CallbackInfo ci) {
        BlockPos blockPos = blockOutlineRenderState.pos();
        Entity entity = this.minecraft.getCameraEntity();
        BlockState blockState = this.level.getBlockState(blockPos);
        if (this.minecraft.player != null && this.minecraft.player.getItemBySlot(EquipmentSlot.MAINHAND)
                .getItem() instanceof BuilderStaffItem staffItem) {
            HitResult hitResult = this.minecraft.hitResult;

            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos lookingAtPos = ((BlockHitResult) hitResult).getBlockPos();
                BlockState lookingAtState = this.level.getBlockState(lookingAtPos);
                Block lookingAtBlock = lookingAtState.getBlock();
                Item item = lookingAtBlock.asItem();
                VoxelShape shape = Shapes.empty();

                // check to make sure the block we're placing off has an item
                if (item != Items.AIR) {
                    // get amount of required item in player inventory
                    int count = 0;
                    for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); i++) {
                        if (this.minecraft.player.getInventory().getItem(i).is(item)) {
                            count += this.minecraft.player.getInventory().getItem(i).getCount();
                        }
                    }
                    if (this.minecraft.player.getOffhandItem().is(item)) {
                        count += this.minecraft.player.getOffhandItem().getCount();
                    }

                    // run placement logic if they have at least 1 of the item (or if they are in
                    // creative)
                    if (count > 0 || this.minecraft.player.isCreative()) {
                        // get number of blocks to place (min between max size and the count of items in
                        // inventory)
                        int maxChecks = Math.min(staffItem.getMaxSize(),
                                this.minecraft.player.isCreative() ? staffItem.getMaxSize() : count);

                        // get initial positions within our selection
                        List<BlockPos> validPositions = SelectionCalculator.calculateSelection(level, lookingAtPos,
                                ((BlockHitResult) hitResult).getDirection(), maxChecks);

                        // add all positions to the overall shape
                        for (BlockPos newPosition : validPositions) {
                            if (this.level.getWorldBorder().isWithinBounds(newPosition)) {
                                BlockPos testPos = lookingAtPos.subtract(newPosition);
                                shape = Shapes.or(shape,
                                        lookingAtState.getShape(this.level, lookingAtPos, CollisionContext.of(entity))
                                                .move(-testPos.getX(), -testPos.getY(), -testPos.getZ()));
                            }
                        }

                        // render shape
                        VertexConsumer linesBuffer = renderBuffers.bufferSource().getBuffer(RenderType.lines());
                        double originX = lookingAtPos.getX() - d;
                        double originY = lookingAtPos.getY() - e;
                        double originZ = lookingAtPos.getZ() - f;

                        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
                            linesBuffer
                                    .addVertex(poseStack.last().pose(), (float) (x1 + originX), (float) (y1 + originY),
                                            (float) (z1 + originZ))
                                    .setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(0.0F, 0.0F, 0.0F);
                            linesBuffer
                                    .addVertex(poseStack.last().pose(), (float) (x2 + originX), (float) (y2 + originY),
                                            (float) (z2 + originZ))
                                    .setColor(0.0F, 0.0F, 0.0F, 0.4F).setNormal(0.0F, 0.0F, 0.0F);
                        });

                        ci.cancel();
                        ci.cancel();
                    }
                }
            }
        }
    }
}
