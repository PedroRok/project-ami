package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.RobotEntity;
import com.pedrorok.ami.entities.robot.behaviors.mining.MiningUtils;
import com.pedrorok.ami.entities.robot.tasks.base.TaskType;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningPlan;
import com.pedrorok.ami.entities.robot.tasks.mining.MiningTaskData;
import com.pedrorok.ami.network.packets.PlayAnimationPacket;
import com.pedrorok.ami.pathfinding.mining.MiningPathPlan;
import com.pedrorok.ami.registry.ModMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.item.DiggerItem;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tslat.smartbrainlib.api.core.behaviour.ExtendedBehaviour;
import net.tslat.smartbrainlib.object.MemoryTest;

import com.mojang.datafixers.util.Pair;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * Behavior que executa o plano de mineração completo.
 * Substitui AdvanceToNextBlock, MoveToMiningTarget e BreakBlock.
 * Consolida TODA a lógica de mineração em UM behavior.
 */
public class ExecuteMiningPlan extends ExtendedBehaviour<RobotEntity> {
    
    public static final MemoryTest MEMORY_REQUIREMENTS = MemoryTest.builder(2)
        .hasMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
        .hasMemory(ModMemoryModuleTypes.MINING_PLAN.get());
    
    private int breakingProgress = 0;
    private BlockPos lastAttemptedBlock = null;
    private int failedAttempts = 0;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final double MAX_MINING_DISTANCE = 25.0; // 5 blocks squared
    private static final int BREAK_TIME = 20; // 1 segundo
    
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
                    // Só executar se MINING
                    boolean isMining = miningTask.getPhase() == MiningTaskData.MiningPhase.MINING;
                    ProjectAmi.LOGGER.debug("[ExecuteMiningPlan] checkStart: phase={}, isMining={}", 
                        miningTask.getPhase(), isMining);
                    return isMining;
                }
                return false;
            })
            .orElse(false);
        
        if (result) {
            ProjectAmi.LOGGER.info("[ExecuteMiningPlan] INICIANDO execução do plano de mineração");
        }
        
        return result;
    }
    
    /**
     * Encontra o próximo bloco acessível no plano, pulando blocos inacessíveis.
     * Retorna null se não houver mais blocos acessíveis.
     */
    private BlockPos findNextAccessibleBlock(RobotEntity robot, MiningPlan plan, ServerLevel level) {
        // NOVO: Usar octree pathfinding se disponível
        if (plan instanceof MiningPathPlan octreePlan) {
            BlockPos nextBlock = octreePlan.getNextAccessibleBlock(robot);
            if (nextBlock != null) {
                ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Octree pathfinding found accessible block: {}", nextBlock);
                return nextBlock;
            }
        }
        
        // Fallback para implementação antiga
        return findNextAccessibleBlockLegacy(robot, plan, level);
    }
    
    /**
     * Implementação legacy de busca de blocos acessíveis.
     * Mantida como fallback para compatibilidade.
     */
    private BlockPos findNextAccessibleBlockLegacy(RobotEntity robot, MiningPlan plan, ServerLevel level) {
        int maxSkips = 10; // Evitar loop infinito
        int skips = 0;
        
        while (skips < maxSkips) {
            BlockPos candidate = plan.getCurrentBlock();
            
            if (candidate == null) {
                // Plano completo
                return null;
            }
            
            // 1. Verificar se já é ar (já foi minerado)
            if (level.getBlockState(candidate).isAir()) {
                ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Bloco {} já é ar, pulando.", candidate);
                plan.markBlockCompleted(); // Marcar como completo e avançar
                skips++;
                continue;
            }
            
            // 2. Verificar distância
            double distance = robot.distanceToSqr(
                candidate.getX() + 0.5,
                candidate.getY() + 0.5,
                candidate.getZ() + 0.5
            );
            
            if (distance > MAX_MINING_DISTANCE) {
                ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Bloco {} muito longe ({}), pulando.", 
                    candidate, Math.sqrt(distance));
                plan.markBlockCompleted();
                skips++;
                continue;
            }
            
            // 3. Verificar linha de visão
            boolean hasLoS = MiningUtils.hasLineOfSight(robot, candidate, level);
            
            if (!hasLoS) {
                // Tentar encontrar obstrutor
                BlockPos obstructor = MiningUtils.findObstructingBlock(robot, candidate, level);
                
                if (obstructor != null && !obstructor.equals(candidate)) {
                    // Há um obstrutor - minerar ele primeiro
                    ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Bloco {} obstruído por {}. Minerando obstrutor primeiro.", 
                        candidate, obstructor);
                    return obstructor; // Retornar obstrutor como próximo target
                } else {
                    // Sem obstrutor identificável - bloco inacessível
                    ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Bloco {} sem linha de visão e sem obstrutor identificável, pulando.", 
                        candidate);
                    plan.markBlockCompleted();
                    skips++;
                    continue;
                }
            }
            
            // Bloco é acessível!
            ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Bloco acessível encontrado: {}", candidate);
            return candidate;
        }
        
        // Nenhum bloco acessível encontrado após 10 tentativas
        ProjectAmi.LOGGER.error("[ExecuteMiningPlan] Não foi possível encontrar bloco acessível após {} tentativas.", maxSkips);
        return null;
    }
    
    @Override
    protected void start(ServerLevel level, RobotEntity robot, long gameTime) {
        ProjectAmi.LOGGER.info("=== [ExecuteMiningPlan] START ===");
        
        MiningPlan plan = robot.getBrain().getMemory(ModMemoryModuleTypes.MINING_PLAN.get()).orElse(null);
        if (plan == null) {
            ProjectAmi.LOGGER.error("[ExecuteMiningPlan] Plano não encontrado na memória!");
            return;
        }
        
        // 🆕 Usar findNextAccessibleBlock ao invés de getNextBlock
        BlockPos nextBlock = findNextAccessibleBlock(robot, plan, level);
        if (nextBlock == null) {
            ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Nenhum bloco acessível encontrado. Finalizando task.");
            completeMining(robot);
            return;
        }
        
        ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Próximo bloco acessível: {}", nextBlock);
        
        // Setar WALK_TARGET para o bloco
        robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
            new WalkTarget(nextBlock, 1.0F, 0));
        
        breakingProgress = 0;
        lastAttemptedBlock = nextBlock;
        failedAttempts = 0;
    }
    
    @Override
    protected void tick(ServerLevel level, RobotEntity robot, long gameTime) {
        MiningPlan plan = robot.getBrain().getMemory(ModMemoryModuleTypes.MINING_PLAN.get()).orElse(null);
        if (plan == null) return;
        
        // 🆕 Usar findNextAccessibleBlock
        BlockPos currentTarget = findNextAccessibleBlock(robot, plan, level);
        if (currentTarget == null) {
            ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Sem blocos acessíveis. Completando mineração.");
            completeMining(robot);
            return;
        }
        
        // 🆕 Se o target mudou, resetar
        if (!currentTarget.equals(lastAttemptedBlock)) {
            breakingProgress = 0;
            lastAttemptedBlock = currentTarget;
            failedAttempts = 0;
            robot.getBrain().setMemory(MemoryModuleType.WALK_TARGET, 
                new WalkTarget(currentTarget, 1.0F, 0));
        }
        
        // Checar se está perto o suficiente para minerar
        double distance = robot.distanceToSqr(
            currentTarget.getX() + 0.5, 
            currentTarget.getY() + 0.5, 
            currentTarget.getZ() + 0.5
        );
        
        if (distance > 9.0) { // 3 blocks squared
            // Ainda navegando
            breakingProgress = 0;
            
            // 🆕 Detectar se está travado (não consegue chegar)
            failedAttempts++;
            if (failedAttempts > 100) { // ~5 segundos
                ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Robô travado tentando alcançar {}. Pulando bloco.", currentTarget);
                plan.markBlockCompleted();
                failedAttempts = 0;
            }
            
            return;
        }
        
        // 🆕 Validar LoS antes de minerar
        if (!MiningUtils.hasLineOfSight(robot, currentTarget, level)) {
            ProjectAmi.LOGGER.warn("[ExecuteMiningPlan] Perdeu linha de visão para {}. Re-avaliando.", currentTarget);
            breakingProgress = 0;
            return; // Próximo tick irá re-avaliar
        }
        
        // Resetar contador de falhas (conseguiu chegar)
        failedAttempts = 0;
        
        // Está perto E tem LoS - começar a quebrar
        breakingProgress++;
        
        if (breakingProgress % 5 == 0) {
            robot.swing(robot.getUsedItemHand());
            PacketDistributor.sendToPlayersTrackingEntity(robot, new PlayAnimationPacket("use-tool", robot.getId()));
        }
        
        if (breakingProgress >= BREAK_TIME) {
            // Quebrar bloco
            ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Quebrando bloco: {}", currentTarget);
            level.destroyBlock(currentTarget, true);
            
            // Consumir durabilidade da ferramenta
            consumeToolDurability(robot);
            
            // 🆕 Atualizar octree quando bloco é quebrado
            if (plan instanceof MiningPathPlan octreePlan) {
                octreePlan.updateOctree(currentTarget, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            }
            
            // 🆕 Marcar como completo APENAS se era um bloco da task original
            BlockPos originalTarget = plan.getCurrentBlock();
            if (originalTarget != null && originalTarget.equals(currentTarget)) {
                plan.markBlockCompleted();
                ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Bloco da task completado: {}", currentTarget);
            } else {
                ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Bloco obstrutor removido: {}", currentTarget);
            }
            
            // Resetar progresso
            breakingProgress = 0;
            
            // Feedback periódico (a cada 10 blocos)
            if (plan.getCompletedBlocks() % 10 == 0) {
                String message = String.format("<green>[A.M.I.]</green> <gray>Progress: %d/%d</gray>", 
                    plan.getCompletedBlocks(), plan.getTotalBlocks());
                sendMessageToOwner(robot, message);
            }
        }
    }
    
    @Override
    protected boolean shouldKeepRunning(RobotEntity entity) {
        return entity.getBrain()
            .getMemory(ModMemoryModuleTypes.CURRENT_TASK.get())
            .filter(task -> task.type() == TaskType.MINING)
            .map(task -> {
                if (task instanceof MiningTaskData miningTask) {
                    return miningTask.getPhase() == MiningTaskData.MiningPhase.MINING;
                }
                return false;
            })
            .orElse(false);
    }
    
    /**
     * Consome durabilidade da ferramenta
     */
    private void consumeToolDurability(RobotEntity robot) {
        if (robot.getMainHandItem().getItem() instanceof DiggerItem) {
            // Simular consumo de durabilidade
            // TODO: Implementar sistema de energia/durabilidade real
            ProjectAmi.LOGGER.debug("[ExecuteMiningPlan] Consumindo durabilidade da ferramenta");
        }
    }
    
    /**
     * Finaliza a mineração e limpa a memória
     */
    private void completeMining(RobotEntity robot) {
        ProjectAmi.LOGGER.info("[ExecuteMiningPlan] Mineração completa!");
        
        String message = "<green>[A.M.I.]</green> <gray>Mining complete!</gray>";
        sendMessageToOwner(robot, message);
        
        // Limpar memória e voltar para IDLE
        robot.getBrain().eraseMemory(ModMemoryModuleTypes.CURRENT_TASK.get());
        robot.getBrain().eraseMemory(ModMemoryModuleTypes.MINING_PLAN.get());
        robot.getBrain().setActiveActivityIfPossible(net.minecraft.world.entity.schedule.Activity.IDLE);
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
