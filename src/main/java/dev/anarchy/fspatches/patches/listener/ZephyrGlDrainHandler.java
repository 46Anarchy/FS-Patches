package dev.anarchy.fspatches.patches.listener;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import dev.anarchy.fspatches.patches.asm.patches.ZephyrGlFinalizeSafePatch;

/**
 * Vide au tick client les suppressions GL mises en file par les
 * {@code finalize()} de ZephyrUI (voir {@link ZephyrGlFinalizeSafePatch}).
 *
 * <p>Le tick client tourne sur le thread de rendu, contexte GL courant : c'est
 * le seul endroit sûr pour appeler {@code glDeleteTextures} /
 * {@code glDeleteFramebuffers} sous LWJGL3.
 */
public class ZephyrGlDrainHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ZephyrGlFinalizeSafePatch.drain();
    }
}
