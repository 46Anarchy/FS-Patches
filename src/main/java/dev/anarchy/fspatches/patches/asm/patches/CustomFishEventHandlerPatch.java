package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;


public class CustomFishEventHandlerPatch extends BasePatch {
    private static final String TARGET = "fr/paladium/palajobs/core/CommonProxy";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!transformedName.replace('.', '/').equals(TARGET)) return basicClass;

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("onPreInit")) continue;

            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != Opcodes.NEW) continue;
                TypeInsnNode typeInsn = (TypeInsnNode) insn;
                if (!typeInsn.desc.equals("fr/paladium/palajobs/server/listener/CustomFishEventHandler")) continue;

                typeInsn.desc = "dev/anarchy/fspatches/patches/listener/CustomFishEventHandler";

                AbstractInsnNode next = insn.getNext();
                next = next.getNext();
                if (next instanceof MethodInsnNode) {
                    ((MethodInsnNode) next).owner = "dev/anarchy/fspatches/patches/listener/CustomFishEventHandler";
                }

                break;
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
