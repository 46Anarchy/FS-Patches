package dev.anarchy.fspatches.patches.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PatchDispatcher implements net.minecraft.launchwrapper.IClassTransformer {

    private static volatile List<IClassTransformer> transformers;

    public static Set<Class<? extends BasePatch>> findAll() {
        Reflections reflections = new Reflections(
                "dev.anarchy.fspatches.patches.asm.patches"
        );

        return reflections.getSubTypesOf(BasePatch.class);
    }

    private static void init() {
        if (transformers != null) return;

        transformers = new ArrayList<>();

        Set<Class<? extends BasePatch>> found = PatchDispatcher.findAll();

        for (Class<? extends BasePatch> c : found) {
            try {
                BasePatch patch = c.newInstance();
                transformers.add(patch);
                System.out.println("[FSASM] Loaded patch: " + c.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        init();

        byte[] data = basicClass;

        for (IClassTransformer t : transformers) {
            data = t.transform(name, transformedName, data);
        }

        return data;
    }
}
