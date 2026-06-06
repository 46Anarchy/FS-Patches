package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import lombok.SneakyThrows;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PProxyPalamodRemoveLauncherCheckAgain extends BasePatch {
    @SneakyThrows
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (!"fr.paladium.palamod.client.PProxyClient".equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("initialize")) {
                InsnList insns = method.instructions;

                for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.IF_ACMPEQ) {
                        insns.set(insn, new JumpInsnNode(Opcodes.IF_ACMPNE, ((JumpInsnNode)insn).label));
                    }

                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
