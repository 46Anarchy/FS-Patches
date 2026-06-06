package dev.anarchy.fspatches.patches.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import dev.anarchy.fspatches.updater.CDNSTATUS;
import dev.anarchy.fspatches.updater.FileIntegrity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Sometime, just sometime, mixins aren't enough
// and that's when things get REALLY ugly even for Java standards
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("FSASM")
public class FSAsm implements IFMLLoadingPlugin {

    public static CDNSTATUS status = CDNSTATUS.NOTHING_YET;

    @Override
    public void injectData(Map<String, Object> data) {
        if (status == CDNSTATUS.NOTHING_YET)
            status = FileIntegrity.areFilesPresent();
    }

    @Override
    public String[] getASMTransformerClass() {
        Set<String> patches = new HashSet<>();
/*
I'm sorry system, you have outlived your utility o7
        new Reflections(
            new ConfigurationBuilder()
                    .forPackages("dev.anarchy.fspatches.patches.asm.patches")
                    .setScanners(new SubTypesScanner(false)))
            .getSubTypesOf(BasePatch.class)
            .forEach(clazz -> {
                if (!patches.contains(clazz.getName())) {
                    System.out.println("Adding patch " + clazz.getName());
                    patches.add(clazz.getName());
                }
            });
        return patches.toArray(new String[0]);*/
        return new String[] {
            "dev.anarchy.fspatches.patches.asm.PatchDispatcher"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return MixinLoader.class.getName();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
