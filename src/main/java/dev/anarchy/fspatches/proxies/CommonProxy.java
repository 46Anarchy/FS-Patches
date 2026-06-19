package dev.anarchy.fspatches.proxies;

import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {
    public CommonProxy() {

    }

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}