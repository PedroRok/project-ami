package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningPlan;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;

import com.mojang.datafixers.util.Pair;

import java.util.List;

/**
 * Behavior que executa APENAS UMA VEZ para planejar toda a mineração.
 * Cria um MiningPlan completo e avança para a fase NAVIGATING.
 */
public class PlanMiningTask extends ExtendedBehaviour<RobotEntity> {
    
    public static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(1)
        .hasMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
    
    @Override
    protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
        return MEMORY_REQUIREMENTS;
    }
    
    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, RobotEntity robot) {
        boolean result = robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .filter(task -> task.type() == TaskType.MINING)
            .map(task -> {
                if (task instanceof MiningTaskData miningTask) {
                    // Só executar se phase == PLANNING
                    boolean isPlanning = miningTask.getPhase() == MiningTaskData.MiningPhase.PLANNING;
                    ProjectAmi.LOGGER.debug("[PlanMiningTask] checkStart: phase={}, isPlanning={}", 
                        miningTask.getPhase(), isPlanning);
                    return isPlanning;
                }
                return false;
            })
            .orElse(false);
        
        if (result) {
            ProjectAmi.LOGGER.info("[PlanMiningTask] INICIANDO planejamento da mineração");
        }
        
        return result;
    }
    
    @Override
    protected void start(ServerLevel level, RobotEntity robot, long gameTime) {
        ProjectAmi.LOGGER.info("=== [PlanMiningTask] START ===");
        
        robot.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .ifPresent(task -> {
                if (task instanceof MiningTaskData miningTask) {
                    ProjectAmi.LOGGER.info("[PlanMiningTask] Criando plano para task: direction={}, blocks={}, pattern={}", 
                        miningTask.getDirection(), miningTask.getTotalBlocks(), miningTask.getPattern());
                    
                    // 1. Criar plano de mineração
                    MiningPlan plan = MiningPlan.createFromTask(miningTask, level);
                    
                    // 2. Validar plano
                    if (!plan.isViable()) {
                        // Cancelar task e notificar jogador
                        String message = "<red>[A.M.I.]</red> <gray>" + plan.getFailureReason() + "</gray>";
                        sendMessageToOwner(robot, message);
                        
                        ProjectAmi.LOGGER.warn("[PlanMiningTask] Plano inviável: {}", plan.getFailureReason());
                        
                        // Limpar memória e voltar para IDLE
                        robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
                        robot.getBrain().eraseMemory(ModMemoryModuleTypes.MINING_PLAN.get());
                        robot.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
                        return;
                    }
                    
                    // 3. Salvar plano na memória
                    robot.getBrain().setMemory(ModMemoryModuleTypes.MINING_PLAN.get(), plan);
                    
                    // 4. Avançar para fase NAVIGATING
                    miningTask.setPhase(MiningTaskData.MiningPhase.NAVIGATING);
                    
                    // 5. Feedback ao jogador
                    String message = String.format("<green>[A.M.I.]</green> <gray>Plan ready! Mining %d blocks.</gray>", 
                        plan.getTotalBlocks());
                    sendMessageToOwner(robot, message);
                    
                    ProjectAmi.LOGGER.info("[PlanMiningTask] Plano criado com sucesso: {} blocos, {} obstáculos", 
                        plan.getTotalBlocks(), plan.getObstaclesInPath().size());
                }
            });
        
        // Este behavior só executa UMA VEZ
        this.doStop(level, robot, gameTime);
    }
    
    @Override
    protected boolean shouldKeepRunning(RobotEntity entity) {
        // Este behavior nunca deve continuar rodando
        return false;
    }
    
    /**
     * Envia mensagem para o dono do robô
     */
    private void sendMessageToOwner(RobotEntity robot, String message) {
        if (robot.getOwner() != null) {
            robot.getOwner().sendSystemMessage(Component.literal(message));
        }
    }
}
