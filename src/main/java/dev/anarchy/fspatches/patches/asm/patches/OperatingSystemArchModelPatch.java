package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import dev.anarchy.fspatches.patches.asm.ModernRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.util.Locale;

import static org.objectweb.asm.Opcodes.*;

public class OperatingSystemArchModelPatch extends BasePatch {

    private static final String SELF_INTERNAL = "dev/anarchy/fspatches/patches/asm/patches/OperatingSystemArchModelPatch";
    private static final String TARGET = "fr.paladium.palamod.client.utils.OperatingSystem";

    public static String archDataModel(String preferredKey) {
        try {
            String model = System.getProperty(preferredKey);
            if (model != null) return model;
        } catch (Throwable ignored) {
        }
        String arch = "";
        try {
            arch = String.valueOf(System.getProperty("os.arch", ""));
        } catch (Throwable ignored) {
        }
        return arch.toLowerCase(Locale.ENGLISH).contains("64") ? "64" : "32";
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        // Match the target first: calling into ModernRuntime while it is itself being
        // transformed re-enters the class loader and dies with ClassCircularityError.
        if (!TARGET.equals(transformedName)) return basicClass;
        // Java 8 players keep the original classes untouched (see ModernRuntime).
        if (!ModernRuntime.isLwjgl3ify()) return basicClass;
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.getOpcode() == INVOKESTATIC && call.owner.equals("java/lang/System") && call.name.equals("getProperty")) {
                    AbstractInsnNode prev = call.getPrevious();
                    while (prev instanceof LabelNode || prev instanceof LineNumberNode || prev instanceof FrameNode) prev = prev.getPrevious();
                    if (prev instanceof LdcInsnNode && "sun.arch.data.model".equals(((LdcInsnNode) prev).cst)) {
                        method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF_INTERNAL, "archDataModel", "(Ljava/lang/String;)Ljava/lang/String;", false));
                        patched = true;
                    }
                }
            }
        }
        if (!patched) return basicClass;
        System.out.println("[FSASM] OperatingSystemArchModelPatch applied to " + transformedName);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
