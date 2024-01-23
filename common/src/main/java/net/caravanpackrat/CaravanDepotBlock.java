package net.caravanpackrat;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

public class CaravanDepotBlock extends BlockWithEntity {

    public static final VoxelShape SHAPE = VoxelShapes.cuboid(0,0,0,16/16f,8/16f,16/16f);

    public CaravanDepotBlock(Settings properties) {
        super(properties);
    }

    @Override
    public @NotNull ImmutableMap<BlockState, VoxelShape> getShapesForStates(Function<BlockState,VoxelShape> map) {
        return super.getShapesForStates((blockState) -> SHAPE);
    }

    @Override
    public @NotNull VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context){
        return SHAPE;
    }

    @Override
    public @NotNull BlockRenderType getRenderType(BlockState state){
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CaravanDepotBlockEntity(pos, state);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CaravanDepotBlockEntity depotBlockEntity) {
                depotBlockEntity.onBreak(world, pos, state, player);
            }
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CaravanDepotBlockEntity depotBlockEntity) {
                var entityResult = depotBlockEntity.onUse(player, hand, hit);
                if (entityResult.isAccepted()) {
                    return entityResult;
                }

                player.openHandledScreen(depotBlockEntity);
            }

        }
        return ActionResult.success(world.isClient);
    }
}
