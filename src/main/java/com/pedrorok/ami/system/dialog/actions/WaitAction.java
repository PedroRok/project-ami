package com.pedrorok.ami.system.dialog.actions;

import com.pedrorok.ami.ProjectAmi;

/**
 * Ação para fazer o diálogo aguardar um determinado tempo (baseado em ticks)
 * @author Rok, Pedro Lucas nmm. Created on 11/10/2025
 * @project project-ami
 */
public class WaitAction implements DialogueAction {
    
    private final int waitTicks;
    private int currentTicks = 0;
    private boolean isWaiting = false;
    
    public WaitAction() {
        this.waitTicks = 20; // 1 segundo = 20 ticks
    }
    
    public WaitAction(int seconds) {
        this.waitTicks = Math.max(1, seconds) * 20; // Converte segundos para ticks
    }
    
    @Override
    public boolean execute(ActionContext context) {
        // Inicia a espera baseada em ticks
        this.isWaiting = true;
        this.currentTicks = 0;
        ProjectAmi.LOGGER.debug("WaitAction: Iniciando espera de {} ticks ({} segundos)", waitTicks, waitTicks / 20);
        return true;
    }
    
    /**
     * Atualiza o contador de ticks - deve ser chamado a cada tick
     * @return true se ainda está esperando, false se terminou
     */
    public boolean updateTicks() {
        if (!isWaiting) {
            return false;
        }
        
        currentTicks++;
        if (currentTicks >= waitTicks) {
            isWaiting = false;
            ProjectAmi.LOGGER.debug("WaitAction: Espera concluída após {} ticks", currentTicks);
            return false;
        }
        
        return true;
    }
    
    /**
     * Verifica se ainda está esperando
     */
    public boolean isWaiting() {
        return isWaiting;
    }
    
    @Override
    public String processText(String text, String command) {
        // Remove o comando do texto
        return text.replace("<" + command + ">", "");
    }
    
    @Override
    public String getCommandPattern() {
        return "wait_\\d+";
    }
    
    /**
     * Cria uma nova instância da ação com o tempo específico
     */
    public static WaitAction forSeconds(int seconds) {
        return new WaitAction(seconds);
    }
    
    public int getWaitTicks() {
        return waitTicks;
    }
    
    public int getCurrentTicks() {
        return currentTicks;
    }
}
