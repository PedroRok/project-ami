package com.pedrorok.ami.entities.robot.tasks.mining;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pedrorok.ami.ProjectAmi;
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

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import java.util.Optional;

public class MiningTaskData implements TaskData {
	
	public enum MiningPhase implements StringRepresentable {
		PLANNING("planning"),      // Avaliando e planejando a task
		NAVIGATING("navigating"),  // Indo até o local de mineração
		MINING("mining"),          // Minerando
		COMPLETED("completed");    // Task finalizada
		
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
			IntProvider.CODEC.fieldOf("totalBlocks").forGetter(d -> ConstantInt.of(d.totalBlocks)),
			IntProvider.CODEC.fieldOf("minedBlocks").forGetter(d -> ConstantInt.of(d.minedBlocks)),
			BlockPos.CODEC.optionalFieldOf("currentTarget").forGetter(d -> Optional.ofNullable(d.currentTarget)),
			StringRepresentable.fromEnum(MiningPattern::values).fieldOf("pattern").forGetter(d -> d.pattern),
			BlockPos.CODEC.fieldOf("startPos").forGetter(d -> d.startPos),
			StringRepresentable.fromEnum(MiningPhase::values).optionalFieldOf("phase", MiningPhase.PLANNING).forGetter(d -> d.phase)
		).apply(instance, (direction, totalBlocksProvider, minedBlocksProvider, currentTarget, pattern, startPos, phase) -> {
			MiningTaskData data = new MiningTaskData(direction, totalBlocksProvider.sample(RandomSource.create()), pattern, startPos);
			data.minedBlocks = minedBlocksProvider.sample(RandomSource.create());
			data.currentTarget = currentTarget.orElse(null);
			data.phase = phase;
			return data;
		})
	);
	
	private final Direction direction;
	@Setter private int totalBlocks;
	private int minedBlocks;
	@Setter private BlockPos currentTarget;
	private final MiningPattern pattern;
	private final BlockPos startPos;
	@Setter private MiningPhase phase = MiningPhase.PLANNING;
	
	public MiningTaskData(Direction direction, int totalBlocks, MiningPattern pattern, BlockPos startPos) {
		this.direction = direction;
		this.totalBlocks = totalBlocks;
		this.pattern = pattern;
		this.startPos = startPos;
		this.minedBlocks = 0;
		this.currentTarget = null;
	}
	
	@Override
	public TaskType type() {
		return TaskType.MINING;
	}
	
	@Override
	public boolean canStart(RobotEntity robot) {
		return /*robot.hasEnergy(50) && */
			   robot.getMainHandItem().getItem() instanceof DiggerItem &&
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
		
		/*if (robot.isOutOfEnergy()) {
			return TaskResult.FAILURE;
		}*/
		
		return TaskResult.RUNNING;
	}
	
	@Override
	public void stop(RobotEntity robot) {
		this.currentTarget = null;
	}
	
	// Getters
	public Direction getDirection() { return direction; }
	public int getTotalBlocks() { return totalBlocks; }
	public int getMinedBlocks() { return minedBlocks; }
	public BlockPos getCurrentTarget() { return currentTarget; }
	public MiningPattern getPattern() { return pattern; }
	public BlockPos getStartPos() { return startPos; }
	public MiningPhase getPhase() { return phase; }
	
	// Business logic
	public boolean isComplete() {
		return minedBlocks >= totalBlocks;
	}
	
	public void incrementProgress() {
		this.minedBlocks++;
	}
	
	public BlockPos calculateNextTarget(RobotEntity robot) {
		BlockPos offset = new BlockPos(
			direction.getStepX() * minedBlocks,
			direction.getStepY() * minedBlocks,
			direction.getStepZ() * minedBlocks
		);
		
		BlockPos result = switch (pattern) {
			case STRAIGHT -> startPos.offset(offset);
			case TUNNEL_2X1 -> calculateTunnelTarget(startPos.offset(offset), 2);
			case TUNNEL_3X3 -> calculateTunnelTarget(startPos.offset(offset), 3);
			case STAIRCASE -> calculateStaircaseTarget(startPos.offset(offset));
			case BRANCH -> calculateBranchTarget(startPos.offset(offset));
		};
		
		ProjectAmi.LOGGER.debug("[MiningTaskData] calculateNextTarget: minedBlocks={}, offset={}, result={}", minedBlocks, offset, result);
		return result;
	}
	
	private BlockPos calculateTunnelTarget(BlockPos basePos, int width) {
		if (width == 2) {
			// Túnel 2x1: alternar entre linha principal e superior
			if (minedBlocks % 2 == 0) {
				return basePos;
			} else {
				return basePos.above();
			}
		} else if (width == 3) {
			// Túnel 3x3: minerar em um padrão 3x3
			// 0,1,2 = linha esquerda | 3,4,5 = linha centro | 6,7,8 = linha direita
			int blockInRow = minedBlocks % 9;
			
			Direction perpendicular = direction.getClockWise();
			
			if (blockInRow < 3) {
				// Linha esquerda (-1 perpendicular)
				int height = blockInRow; // 0=base, 1=meio, 2=topo
				return basePos.relative(perpendicular.getOpposite()).above(height);
			} else if (blockInRow < 6) {
				// Linha centro (sem deslocamento lateral)
				int height = blockInRow - 3;
				return basePos.above(height);
			} else {
				// Linha direita (+1 perpendicular)
				int height = blockInRow - 6;
				return basePos.relative(perpendicular).above(height);
			}
		}
		
		return basePos;
	}
	
	private BlockPos calculateStaircaseTarget(BlockPos basePos) {
		// Escada descendente - a cada 3 blocos desce 1
		if (minedBlocks % 3 == 0) {
			return basePos.below();
		}
		return basePos;
	}
	
	private BlockPos calculateBranchTarget(BlockPos basePos) {
		// Branch mining - criar galhos laterais a cada 5 blocos
		int cycle = minedBlocks % 10;
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
