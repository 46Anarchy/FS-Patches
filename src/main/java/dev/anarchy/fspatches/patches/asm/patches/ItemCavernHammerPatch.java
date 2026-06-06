package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ItemCavernHammerPatch extends BasePatch {
    public static final String TARGET = "fr.paladium.palaspawner.common.item.ItemCavernHammer";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName) && !TARGET.equals(name)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (!method.name.equals("onBlockStartBreak")) continue;

            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != Opcodes.IF_ACMPEQ) continue;

                AbstractInsnNode prev = prevReal(insn.getPrevious());
                if (prev == null || prev.getOpcode() != Opcodes.GETSTATIC) continue;
                FieldInsnNode fin = (FieldInsnNode) prev;
                if (!fin.owner.equals("fr/paladium/ServerType") || !fin.name.equals("MINAGE")) continue;

                AbstractInsnNode getServerType = prevReal(prev.getPrevious());
                AbstractInsnNode getConfig     = getServerType != null ? prevReal(getServerType.getPrevious()) : null;
                AbstractInsnNode getInstance   = getConfig     != null ? prevReal(getConfig.getPrevious())     : null;

                if (getInstance  != null) method.instructions.set(getInstance,  new InsnNode(Opcodes.NOP));
                if (getConfig    != null) method.instructions.set(getConfig,     new InsnNode(Opcodes.NOP));
                if (getServerType!= null) method.instructions.set(getServerType, new InsnNode(Opcodes.NOP));
                method.instructions.set(prev, new InsnNode(Opcodes.NOP));

                LabelNode target = ((JumpInsnNode) insn).label;
                method.instructions.set(insn, new JumpInsnNode(Opcodes.GOTO, target));

                System.out.println("[Fspatches] CavernHammer ServerType check patched");
                break;
            }
            break;
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }

    private AbstractInsnNode prevReal(AbstractInsnNode n) {
        while (n != null && n.getOpcode() == -1) n = n.getPrevious();
        return n;
    }
}