package com.pedrorok.ami.entities.robot.behaviors.mining;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MiningUtils {
    /**
     * Checa se uma entidade tem linha de visão direta para um bloco
     */
	/**
	 * Encontra o bloco que está obstruindo a linha de visão entre a entidade e o target.
	 * @return A posição do bloco obstrutor, ou null se houver linha de visão direta.
	 */
	public static BlockPos findObstructingBlock(Entity entity, BlockPos target, Level level) {
		Vec3 entityEyes = entity.getEyePosition();
		Vec3 targetCenter = Vec3.atCenterOf(target);
		
		BlockHitResult hit = level.clip(new ClipContext(
			entityEyes,
			targetCenter,
			ClipContext.Block.COLLIDER,
			ClipContext.Fluid.NONE,
			entity
		));
		
		BlockPos hitPos = hit.getBlockPos();
		boolean hasLoS = hitPos.equals(target);
		
		ProjectAmi.LOGGER.debug("[MiningUtils] findObstructingBlock: entity={}, target={}, hit={}, hasLoS={}", 
			entity.blockPosition(), target, hitPos, hasLoS);
		
		if (hasLoS) {
			// Linha de visão direta - sem obstrutor
			return null;
		}
		
		// 🆕 CRIAR BLOCKPOS IMUTÁVEL para evitar bugs com MutableBlockPos
		BlockPos immutableObstructor = hitPos.immutable();
		
		ProjectAmi.LOGGER.info("[MiningUtils] Obstrutor identificado: {} (imutável: {})", hitPos, immutableObstructor);
		return immutableObstructor;
	}

	/**
	 * Verifica se há linha de visão direta (mantido para compatibilidade).
	 */
	public static boolean hasLineOfSight(Entity entity, BlockPos target, Level level) {
		return findObstructingBlock(entity, target, level) == null;
	}
}
