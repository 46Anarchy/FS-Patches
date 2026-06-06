package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class KothMessageRemover extends BasePatch {
    public static final String TARGET = "fr.paladium.factions.server.listeners.DamageListener";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("damageCheck")) {
                for (AbstractInsnNode insn : method.instructions.toArray()) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode min = (MethodInsnNode) insn;
                        if (min.owner.equals("fr/paladium/factions/FactionLogger")
                                && min.name.equals("warn")
                                && min.desc.equals("(Ljava/lang/String;)V")) {
                            method.instructions.insertBefore(insn, new InsnNode(Opcodes.POP));
                            method.instructions.remove(insn);
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }
}