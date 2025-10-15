package com.pedrorok.ami.client.gui;

import com.pedrorok.ami.entities.robot.IHaveEnergy;
import com.pedrorok.ami.entities.robot.RobotEnergy;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/10/2025
 * @project project-ami
 */
public class DialogueScreenEntityModule {

    public static void renderEntity(LivingEntity entity, GuiGraphics guiGraphics, int boxX, int boxY, int boxHeight, int mouseX, int mouseY, Font font) {
        if (entity == null) {
            guiGraphics.drawString(font, "No Entity", boxX - 80, boxY + 10, 0xFF0000, false);
            return;
        }
        int entityX = boxX - 60; // Posição X (à esquerda da caixa)
        int entityY = boxY + boxHeight - 50; // Posição Y (base da caixa)
        int entitySize = 50; // Tamanho da entidade

        // Fundo para a entidade (opcional)
        int bgWidth = 90;
        guiGraphics.fill(entityX - bgWidth / 2, boxY, entityX + bgWidth / 2, boxY + boxHeight, 0x88000000);
        guiGraphics.fill(entityX - bgWidth / 2 + 2, boxY + 2, entityX + bgWidth / 2 - 2, boxY + boxHeight - 2, 0x55000000);

        // Debug visual
        guiGraphics.drawString(font, "AMI 0" + entity.getId(), entityX - 40, boxY - 15, 0xFFFFFF, false);

        // Energy (na lateral direita da caixa da entidade, aparece o item que está segurando e a energia)
        if (entity.getMainHandItem() != null && !entity.getMainHandItem().
                isEmpty()) {
            guiGraphics.renderItem(entity.getMainHandItem(), entityX - 42, boxY + boxHeight - 20);
        }
        // Textura de bateria, quando hover aparece a
        if (entity instanceof IHaveEnergy energyEntity) {
            RobotEnergy energy = energyEntity.getEnergy();
            float energyPercent = energy.getEnergyPercentage();
            int barHeight = 40;
            int barWidth = 6;
            int barX = entityX + 30;
            int barY = boxY + boxHeight - 10 - barHeight;
            guiGraphics.fill(barX, barY, barX + barWidth, boxY + boxHeight - 10, 0xFF000000);
            int filledHeight = (int) (barHeight * energyPercent);
            int filledY = barY + (barHeight - filledHeight);
            int barColor = energyPercent > 0.5f ? 0xFF00FF00 : (energyPercent > 0.2f ? 0xFFFFFF00 : 0xFFFF0000);
            guiGraphics.fill(barX + 1, filledY, barX + barWidth - 1, boxY + boxHeight - 10 - 1, barColor);

            if (barX <= mouseX && mouseX <= barX + barWidth && barY <= mouseY && mouseY <= boxY + boxHeight - 10) {

                String energyText = String.format("%d / %d FE", energy.getCurrentEnergy(), energy.getMaxEnergy());
                int textWidth = font.width(energyText);
                guiGraphics.fill(mouseX - 2, mouseY - 10, mouseX + textWidth + 2, mouseY + 10, 0xCC000000);
                guiGraphics.drawString(font, energyText, mouseX, mouseY - 5, 0xFFFFFF, false);
            }


        }

        // Renderiza a entidade olhando para o mouse
        try {
            renderEntity(entity, guiGraphics, entityX, entityY, entitySize, mouseX, mouseY);
        } catch (Exception e) {
            System.out.println("Erro ao renderizar entidade: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Renderiza a entidade na tela (similar ao inventário do jogador)
     */
    private static void renderEntity(LivingEntity entity, GuiGraphics guiGraphics, int x, int y, int size, float mouseX, float mouseY) {
        // Calcula a rotação baseada na posição do mouse
        float xRotation = (float) Math.atan((double) ((y - size / 2) - mouseY) / 40.0);
        float yRotation = (float) Math.atan((double) (x - mouseX) / 40.0);

        // Rotação da entidade
        Quaternionf pose = (new Quaternionf()).rotateZ((float) Math.PI);
        Quaternionf cameraOrientation = (new Quaternionf()).rotateX(xRotation * 20.0F * ((float) Math.PI / 180F));
        pose.mul(cameraOrientation);

        float yBodyRot = entity.yBodyRot;
        float yRot = entity.getYRot();
        float xRot = entity.getXRot();
        float yHeadRotO = entity.yHeadRotO;
        float yHeadRot = entity.yHeadRot;

        // Aplica as rotações
        entity.yBodyRot = 180.0F + yRotation * 20.0F;
        entity.setXRot(-xRotation * 20.0F);
        entity.yHeadRot = 180.0F + yRotation * 20.0F;

        // Renderiza a entidade
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + 0.0625F, 0.0F);
        InventoryScreen.renderEntityInInventory(guiGraphics, x, y, size, vector3f, pose, cameraOrientation, entity);

        // Restaura as rotações originais
        entity.yBodyRot = yBodyRot;
        entity.setYRot(yRot);
        entity.setXRot(xRot);
        entity.yHeadRotO = yHeadRotO;
        entity.yHeadRot = yHeadRot;
    }
}
