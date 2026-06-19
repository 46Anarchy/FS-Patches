package dev.anarchy.fspatches.updater;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;

public class UpdaterTickHandler {
    private boolean triggered = false;
    private int tickDelay = 0;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (triggered) return;
        if (event.phase != TickEvent.Phase.START) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        triggered = true;
        mc.displayGuiScreen(new UpdaterScreen());
    }
}