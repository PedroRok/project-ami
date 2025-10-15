package com.pedrorok.ami.client.gui;

import com.pedrorok.ami.ProjectAmi;
import com.pedrorok.ami.entities.robot.IHaveEnergy;
import com.pedrorok.ami.entities.robot.RobotEnergy;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * @author Rok, Pedro Lucas nmm. Created on 15/10/2025
 * @project project-ami
 */
public class DialogueScreenEntityModule {

    private static final Int2ObjectArrayMap<ResourceLocation> BATERY_TEXTURE = new Int2ObjectArrayMap<>(){
        {
            put(0, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/gui/battery_0.png"));
            put(1, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID,  "textures/gui/battery_1_alert.png"));
            put(2, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/gui/battery_1.png"));
            put(3, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/gui/battery_2.png"));
            put(4, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/gui/battery_3.png"));
            put(5, ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "textures/gui/battery_full.png"));
        }
    };

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
            guiGraphics.renderItem(entity.getMainHandItem(), entityX - 42, boxY + boxHeight - 22);
        }
        // Textura de bateria, quando hover aparece a
        if (entity instanceof IHaveEnergy energyEntity) {
            RobotEnergy energy = energyEntity.getEnergy();
            float energyPercent = energy.getEnergyPercentage();
            int batteryVal = (int) (energyPercent * 5);
            if (batteryVal == 1 || batteryVal == 2) {
                batteryVal = 2;
                if (entity.tickCount % 20 > 10) {
                    batteryVal = 1;
                }
            }

            ResourceLocation batteryTexture = BATERY_TEXTURE.getOrDefault(batteryVal, BATERY_TEXTURE.get(0));
            int barWidth = 32;
            int barHeight = 32;
            int barX = entityX + 26;
            int barY = boxY + (boxHeight - barHeight) + 5;
            guiGraphics.blit(batteryTexture, barX, barY, 0, 0, barWidth, barHeight, barWidth, barHeight);

            if (barX <= mouseX && mouseX <= barX + 16 && barY <= mouseY && mouseY <= boxY + boxHeight) {
                String energyText = String.format("%d", (int) energy.getEnergyPercentage() * 100) + "%";
                int textWidth = font.width(energyText);
                guiGraphics.fill(mouseX - 2 +10, mouseY - 5, mouseX + textWidth + 12, mouseY + 13, 0xCC000000);
                guiGraphics.drawString(font, energyText, mouseX +10, mouseY, 0xFFFFFF, false);
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
