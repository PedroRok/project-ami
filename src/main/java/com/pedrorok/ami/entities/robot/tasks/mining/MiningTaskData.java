package com.pedrorok.ami.entities.robot.tasks.mining;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.base.TaskData;
import com.pedrorok.ami.entities.robot.tasks.base.TaskResult;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.DiggerItem;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

@Slf4j
public class MiningTaskData implements TaskData {
	
	public enum MiningPhase implements StringRepresentable {
		PLANNING("planning"),
		NAVIGATING("navigating"),
		MINING("mining"),
		COMPLETED("completed");
		
		private final String serializedName;
		
		MiningPhase(String serializedName) {
			this.serializedName = serializedName;
		}
		
		@Override
		public @NotNull String getSerializedName() {
			return serializedName;
		}
	}
	
	public static final MapCodec<MiningTaskData> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			Direction.CODEC.fieldOf("direction").forGetter(d -> d.direction),
			IntProvider.CODEC.fieldOf("distance").forGetter(d -> ConstantInt.of(d.distance)),
			IntProvider.CODEC.fieldOf("currentDistance").forGetter(d -> ConstantInt.of(d.currentDistance)),
			BlockPos.CODEC.optionalFieldOf("currentTarget").forGetter(d -> Optional.ofNullable(d.currentTarget)),
			StringRepresentable.fromEnum(MiningPattern::values).fieldOf("pattern").forGetter(d -> d.pattern),
			BlockPos.CODEC.fieldOf("startPos").forGetter(d -> d.startPos),
			StringRepresentable.fromEnum(MiningPhase::values).optionalFieldOf("phase", MiningPhase.PLANNING).forGetter(d -> d.phase)
		).apply(instance, (direction, distanceProvider, currentDistanceProvider, currentTarget, pattern, startPos, phase) -> {
			MiningTaskData data = new MiningTaskData(direction, distanceProvider.sample(RandomSource.create()), pattern, startPos);
			data.currentDistance = currentDistanceProvider.sample(RandomSource.create());
			data.currentTarget = currentTarget.orElse(null);
			data.phase = phase;
			return data;
		})
	);
	
	@Getter private final Direction direction;
	@Getter @Setter private int distance;
	@Getter private int currentDistance;
	private int minedBlocksInCurrentLayer;
	@Getter @Setter private BlockPos currentTarget;
	@Getter private final MiningPattern pattern;
	@Getter private final BlockPos startPos;
	@Getter @Setter private MiningPhase phase = MiningPhase.PLANNING;
	
	public MiningTaskData(Direction direction, int distance, MiningPattern pattern, BlockPos startPos) {
		this.direction = direction;
		this.distance = distance;
		this.pattern = pattern;
		this.startPos = startPos;
		this.currentDistance = 0;
		this.minedBlocksInCurrentLayer = 0;
		this.currentTarget = null;
	}
	
	@Override
	public TaskType type() {
		return TaskType.MINING;
	}
	
	@Override
	public boolean canStart(RobotEntity robot) {
		return robot.getMainHandItem().getItem() instanceof DiggerItem &&
			   !robot.getBrain().hasMemoryValue(com.pedrorok.ami.registry.ModMemoryModuleTypes.CURRENT_TASK.get());
	}
	
	@Override
	public void start(RobotEntity robot) {
		this.currentTarget = calculateNextTarget(robot);
	}
	
	@Override
	public TaskResult tick(RobotEntity robot) {
		if (isComplete()) {
			return TaskResult.COMPLETE;
		}
		
		return TaskResult.RUNNING;
	}
	
	@Override
	public void stop(RobotEntity robot) {
		this.currentTarget = null;
	}
	
	public boolean isComplete() {
		return currentDistance >= distance;
	}
	
	public void incrementProgress(RobotEntity robot) {
		this.minedBlocksInCurrentLayer++;
		
		int blocksPerLayer = MiningConfig.getBlocksPerLayer(pattern, robot);
		if (minedBlocksInCurrentLayer >= blocksPerLayer) {
			this.currentDistance++;
			this.minedBlocksInCurrentLayer = 0;
		}
	}
	
	public BlockPos calculateNextTarget(RobotEntity robot) {
		BlockPos layerBase = startPos.offset(
			direction.getStepX() * currentDistance,
			direction.getStepY() * currentDistance,
			direction.getStepZ() * currentDistance
		);
		
		return switch (pattern) {
			case STRAIGHT -> layerBase;
			case TUNNEL_2X1 -> calculateTunnelBlock(layerBase, 2, minedBlocksInCurrentLayer, robot);
			case TUNNEL_3X3 -> calculateTunnelBlock(layerBase, 3, minedBlocksInCurrentLayer, robot);
			case STAIRCASE -> calculateStaircaseBlock(layerBase, currentDistance, robot);
			case BRANCH -> calculateBranchBlock(layerBase, currentDistance, robot);
		};
	}
	
	private BlockPos calculateTunnelBlock(BlockPos basePos, int width, int blockIndex, RobotEntity robot) {
		int robotHeight = MiningConfig.getRobotHeight(robot);
		int robotWidth = MiningConfig.getRobotWidth(robot);
		
		int effectiveWidth = Math.max(width, robotWidth);
		int effectiveHeight = Math.max(width, robotHeight);
		
		if (effectiveWidth == 2) {
			if (blockIndex % 2 == 0) {
				return basePos;
			} else {
				return basePos.above();
			}
		} else if (effectiveWidth == 3) {
			int blocksPerLayer = effectiveWidth * effectiveHeight;
			int blockInRow = blockIndex % blocksPerLayer;
			
			Direction perpendicular = direction.getClockWise();
			int row = blockInRow / effectiveWidth;
			int col = blockInRow % effectiveWidth;
			
			return basePos
				.relative(perpendicular, col - 1)
				.above(row);
		}
		
		return basePos;
	}
	
	private BlockPos calculateStaircaseBlock(BlockPos basePos, int layerIndex, RobotEntity robot) {
		int robotHeight = MiningConfig.getRobotHeight(robot);
		return basePos.below(layerIndex).above(robotHeight - 1);
	}
	
	private BlockPos calculateBranchBlock(BlockPos basePos, int layerIndex, RobotEntity robot) {
		int cycle = layerIndex % 10;
		if (cycle < 3) {
			return basePos.relative(direction.getClockWise(), cycle);
		} else if (cycle < 6) {
			return basePos.relative(direction.getCounterClockWise(), cycle - 3);
		}
		return basePos;
	}
	
	public enum MiningPattern implements StringRepresentable {
		STRAIGHT,
		TUNNEL_2X1,
		TUNNEL_3X3,
		STAIRCASE,
		BRANCH;
		
		public static final String[] NAMES = { "STRAIGHT", "TUNNEL_2X1", "TUNNEL_3X3", "STAIRCASE", "BRANCH" };
		
		@Override
		public @NotNull String getSerializedName() {
			return this.name();
		}
	}
}
