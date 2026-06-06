package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class TutorialPlayerPatch extends BasePatch {
    public static final String TARGET = "fr.paladium.tutorial.common.network.data.TutorialPlayer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("isCreated")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(Opcodes.ICONST_1));
                insn.add(new InsnNode(Opcodes.IRETURN));
                method.instructions.add(insn);
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS |  ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
