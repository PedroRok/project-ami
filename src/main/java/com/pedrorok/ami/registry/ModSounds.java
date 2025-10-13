package com.pedrorok.ami.registry;

import com.pedrorok.ami.ProjectAmi;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @author Rok, Pedro Lucas nmm. Created on 13/10/2025
 * @project project-ami
 */
public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, ProjectAmi.MOD_ID);


    public static final DeferredHolder<SoundEvent, SoundEvent> CHAT_SFX = SOUNDS.register("chat_char_sfx",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ProjectAmi.MOD_ID, "chat_char_sfx")));

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }

}
