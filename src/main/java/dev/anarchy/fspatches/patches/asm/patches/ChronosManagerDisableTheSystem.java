package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.RETURN;

// fuck
public class ChronosManagerDisableTheSystem extends BasePatch {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!transformedName.equalsIgnoreCase("fr.paladium.chronos.server.managers.ChronosManager")) return basicClass;
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        for (MethodNode method : node.methods) {
            // make the ingame event system always return null,
            // since we dont have an actual event system, it throws exceptions around trying to call it.
            if (method.name.equals("getPlanningSync") || method.name.equals("getPlanningStatusSync")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(ACONST_NULL));
                insn.add(new InsnNode(Opcodes.ARETURN));

                method.instructions.add(insn);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
