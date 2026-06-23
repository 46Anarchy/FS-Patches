package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.RETURN;

public class BigBrotherDoNotSendUpdates extends BasePatch {

    private static final String TARGET = "fr.paladium.palamod.modules.paladynamique.PPalaDynamique";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (!TARGET.equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("addGenerated") || method.name.equals("addDestroyed")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(RETURN));
                method.instructions.add(insn);
                patched = true;
            }
        }

        if (!patched)
            return basicClass;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
