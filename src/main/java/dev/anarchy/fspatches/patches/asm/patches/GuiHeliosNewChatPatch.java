package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiHeliosNewChatPatch extends BasePatch {

    public static final String TARGET = "fr.paladium.helios.module.chat.gui.GuiHeliosNewChat";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("deleteChatLine")
                    && method.desc.equals("(Ljava/lang/String;I)V")) {

                AbstractInsnNode[] insns = method.instructions.toArray();

                for (AbstractInsnNode insn : insns) {
                    if (insn instanceof TypeInsnNode) {
                        TypeInsnNode tin = (TypeInsnNode) insn;
                        if (tin.getOpcode() == Opcodes.CHECKCAST
                                && tin.desc.equals("net/minecraft/client/gui/ChatLine")) {

                            AbstractInsnNode astore = tin.getNext();
                            if (!(astore instanceof VarInsnNode
                                    && ((VarInsnNode) astore).getOpcode() == Opcodes.ASTORE))
                                continue;

                            LabelNode skipLabel = new LabelNode();

                            InsnList patch = new InsnList();
                            patch.add(new VarInsnNode(Opcodes.ALOAD, 4));

                            patch.add(new JumpInsnNode(Opcodes.IFNULL, skipLabel));

                            method.instructions.insert(astore, patch);

                            AbstractInsnNode cur = astore.getNext();
                            while (cur != null) {
                                if (cur instanceof JumpInsnNode
                                        && cur.getOpcode() == Opcodes.GOTO) {
                                    LabelNode loopStart = ((JumpInsnNode) cur).label;
                                    method.instructions.insertBefore(loopStart, skipLabel);
                                    break;
                                }
                                cur = cur.getNext();
                            }
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}