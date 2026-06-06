package dev.anarchy.fspatches.patches.asm;

import cpw.mods.fml.relauncher.IFMLCallHook;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

public class MixinLoader implements IFMLCallHook {
    @Override
    public void injectData(Map<String, Object> data) {
        MixinBootstrap.init();
        Mixins.addConfiguration("fspatches.mixin.json");
    }

    @Override
    public Void call() throws Exception {
        return null;
    }
}
