package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.awt.*;

import static org.objectweb.asm.Opcodes.*;

// patch for the GuiUtils class that prevent the game from crashing
// when trying to open the browser on Linux because this is not a supported
// operation on that platform.

// FUCK IT WE BALL
public class GuiUtilsNoCrashOnLinuxTransformer extends BasePatch {

    private static final String TARGET = "fr.paladium.lib.apollon.utils.GuiUtils";
    private static final String TARGET_INTERNAL = "fr/paladium/lib/apollon/utils/GuiUtils";
    private static final String SELF_INTERNAL =
            "dev/anarchy/fspatches/patches/asm/patches/GuiUtilsNoCrashOnLinuxTransformer";


    public static void linuxCompliantURLOpener(String url) {
        try {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (!TARGET.equals(transformedName))
            return basicClass;

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("openBrowser")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new VarInsnNode(ALOAD, 0));
                insn.add(new MethodInsnNode(
                        INVOKESTATIC,
                        SELF_INTERNAL,
                        "linuxCompliantURLOpener",
                        "(Ljava/lang/String;)V",
                        false
                ));
                insn.add(new InsnNode(RETURN));
                method.instructions.add(insn);
                method.maxStack = 1;
                method.maxLocals = 1;
                patched = true;
            }
        }

        if (!patched)
            return basicClass;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}