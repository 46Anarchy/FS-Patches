package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import dev.anarchy.fspatches.patches.asm.ModernRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import static org.objectweb.asm.Opcodes.*;

public class ClipboardHeadlessSafePatch extends BasePatch {

    private static final String SELF_INTERNAL = "dev/anarchy/fspatches/patches/asm/patches/ClipboardHeadlessSafePatch";
    private static final String ZEPHYR = "fr.paladium.zephyrui.lib.utils.clipboard.ClipboardUtils";
    private static final String APOLLON = "fr.paladium.lib.apollon.utils.ClipboardUtils";

    public static Toolkit safeToolkit() {
        try {
            return Toolkit.getDefaultToolkit();
        } catch (Throwable t) {
            return null;
        }
    }

    public static Clipboard safeClipboard(Toolkit toolkit) {
        if (toolkit == null) return null;
        try {
            return toolkit.getSystemClipboard();
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        // Match the targets first: calling into ModernRuntime while it is itself being
        // transformed re-enters the class loader and dies with ClassCircularityError.
        if (!ZEPHYR.equals(transformedName) && !APOLLON.equals(transformedName)) return basicClass;
        // Java 8 players keep the original classes untouched (see ModernRuntime).
        if (!ModernRuntime.isLwjgl3ify()) return basicClass;
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.getOpcode() == INVOKESTATIC && call.owner.equals("java/awt/Toolkit") && call.name.equals("getDefaultToolkit")) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF_INTERNAL, "safeToolkit", "()Ljava/awt/Toolkit;", false));
                    patched = true;
                } else if (call.getOpcode() == INVOKEVIRTUAL && call.owner.equals("java/awt/Toolkit") && call.name.equals("getSystemClipboard")) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF_INTERNAL, "safeClipboard", "(Ljava/awt/Toolkit;)Ljava/awt/datatransfer/Clipboard;", false));
                    patched = true;
                }
            }
        }
        if (!patched) return basicClass;
        System.out.println("[FSASM] ClipboardHeadlessSafePatch applied to " + transformedName);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
