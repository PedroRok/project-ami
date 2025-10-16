package com.pedrorok.ami.blocks;

import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/10/2025
 * @project project-ami
 */
public class RobotPartsBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty HEAD = BooleanProperty.create("head");
    public static final BooleanProperty LEFT_ARM = BooleanProperty.create("left_arm");
    public static final BooleanProperty RIGHT_ARM = BooleanProperty.create("right_arm");
    public static final BooleanProperty BATTERY = BooleanProperty.create("battery");


    private static final VoxelShape BASE = Block.box(5, 0, 6, 11, 1, 10);
    private static final VoxelShape BODY = Block.box(5, 1, 5.5, 11, 7, 10.5);

    // Shapes opcionais
    private static final VoxelShape HEAD_SHAPE = Shapes.or(
            Block.box(4, 7, 4.5, 5, 13, 11.5),
            Block.box(5, 7, 4.5, 11, 8, 11.5),
            Block.box(5, 8, 10.5, 11, 12, 11.5),
            Block.box(5, 12, 4.5, 11, 14, 11.5),
            Block.box(11, 7, 4.5, 12, 13, 11.5)
    );

    private static final VoxelShape LEFT_ARM_SHAPE = Shapes.or(
            Block.box(4, 4, 7.5, 5, 7, 9.5),
            Block.box(4, 2, 7.5, 5, 4, 9.5)
    );

    private static final VoxelShape RIGHT_ARM_SHAPE = Shapes.or(
            Block.box(11, 4, 7.5, 12, 7, 9.5),
            Block.box(11, 2, 7.5, 12, 4, 9.5)
    );

    public RobotPartsBlock() {
        super(BlockBehaviour.Properties.of().noOcclusion().dynamicShape().isViewBlocking((a, b, c) -> false).strength(2.0f).lightLevel((state) -> 1));
        registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HEAD, false)
                .setValue(LEFT_ARM, false)
                .setValue(RIGHT_ARM, false)
                .setValue(BATTERY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HEAD, LEFT_ARM, RIGHT_ARM, BATTERY);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return this.defaultBlockState()
                    .setValue(FACING, context.getClickedFace());
        }
        Direction direction = player.getDirection().getOpposite();
        return this.defaultBlockState()
                .setValue(FACING, direction);
    }

    // place region
    public InteractionResult tryPlacePart(UseOnContext ctx, BlockState state, @Nullable String item) {

        if (!ctx.getLevel().isClientSide) {
            boolean partFill = switch (item) {
                case "head" -> state.getValue(HEAD);
                case "arm" -> state.getValue(LEFT_ARM) && state.getValue(RIGHT_ARM);
                case "battery" -> state.getValue(BATTERY);
                default -> true;
            };
            if (partFill) {
                tryAssembleRobot(ctx.getLevel(), ctx.getPlayer(), ctx.getClickedPos(), state);
                return InteractionResult.PASS;
            }
            ctx.getItemInHand().shrink(1); // Remove a parte do robô do inventário do jogador

            BlockState newState = switch (item) {
                case "head" -> state.setValue(HEAD, true);
                case "arm" -> {
                    if (!state.getValue(LEFT_ARM)) {
                        yield state.setValue(LEFT_ARM, true);
                    } else {
                        yield state.setValue(RIGHT_ARM, true);
                    }
                }
                case "battery" -> state.setValue(BATTERY, true);
                default -> state;
            };
            ctx.getLevel().setBlock(ctx.getClickedPos(), newState, 3);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private void tryAssembleRobot(Level level, Player player, BlockPos pos, BlockState state) {
        if (!state.getValue(HEAD) || !state.getValue(LEFT_ARM) || !state.getValue(RIGHT_ARM) || !state.getValue(BATTERY))
            return;
        level.removeBlock(pos, false);

        RobotEntity robot = new RobotEntity(ModEntities.ROBOT.get(), level);
        Vec3 center = pos.getCenter();
        robot.moveTo(center.x, center.y-0.5, center.z, state.getValue(FACING).toYRot(), 0);
        robot.yHeadRot = robot.getYRot();
        robot.setXRot(0);
        level.addFreshEntity(robot);
        robot.setOwner(player);
        robot.setYRot(90);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        tryAssembleRobot(level, player, pos, state);
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
    //endregion



    //shape region
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.or(BASE, BODY);

        if (state.getValue(HEAD)) shape = Shapes.or(shape, HEAD_SHAPE);
        if (state.getValue(LEFT_ARM)) shape = Shapes.or(shape, LEFT_ARM_SHAPE);
        if (state.getValue(RIGHT_ARM)) shape = Shapes.or(shape, RIGHT_ARM_SHAPE);
        //if (state.getValue(REACTOR)) shape = Shapes.or(shape, REACTOR_SHAPE);

        return rotateShape(shape, state.getValue(FACING));
    }

    private VoxelShape rotateShape(VoxelShape shape, Direction direction) {
        if (direction == Direction.NORTH) return shape;

        VoxelShape[] result = new VoxelShape[]{Shapes.empty()};

        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double x1 = rotateX(minX, minY, minZ, direction);
            double y1 = rotateY(minX, minY, minZ, direction);
            double z1 = rotateZ(minX, minY, minZ, direction);
            double x2 = rotateX(maxX, maxY, maxZ, direction);
            double y2 = rotateY(maxX, maxY, maxZ, direction);
            double z2 = rotateZ(maxX, maxY, maxZ, direction);

            result[0] = Shapes.or(result[0], Shapes.box(
                    Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                    Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)
            ));
        });

        return result[0];
    }

    private double rotateX(double x, double y, double z, Direction direction) {
        return switch (direction) {
            case SOUTH -> 1 - x;
            case WEST -> z;
            case EAST -> 1 - z;
            default -> x;
        };
    }

    private double rotateY(double x, double y, double z, Direction direction) {
        return switch (direction) {
            case DOWN -> 1 - z;
            case UP -> z;
            default -> y;
        };
    }

    private double rotateZ(double x, double y, double z, Direction direction) {
        return switch (direction) {
            case DOWN -> y;
            case UP -> 1 - y;
            case SOUTH -> 1 - z;
            case WEST -> 1 - x;
            case EAST -> x;
            default -> z;
        };
    }
    //endregion
}
