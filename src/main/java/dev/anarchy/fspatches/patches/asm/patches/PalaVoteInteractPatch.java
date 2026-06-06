package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class PalaVoteInteractPatch extends BasePatch {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!transformedName.equals("fr.paladium.palavote.server.listener.PalaVoteInteractServerListener")) {
            return basicClass;
        }

        System.out.println("[PalaVoteInteractPatch] Transforming " + name + " to " + transformedName);

        ClassReader cr = new ClassReader(basicClass);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        for (MethodNode mn : cn.methods) {
            if (mn.name.equals("onInteract")) {
                mn.instructions.clear();
                mn.tryCatchBlocks.clear();
                mn.instructions.add(new InsnNode(Opcodes.RETURN));
                break;
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }
}