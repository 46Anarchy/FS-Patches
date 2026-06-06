package dev.anarchy.fspatches.patches.asm.patches;


import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

// delete a check in the RenderHandler to revert the pause menu from the paladium one
// to the vanilla one. Part of the replay mod compatibility code.
public class RenderHandlerDontReplaceVanillaInGameMenu extends BasePatch {

    private static final String TARGET = "fr.paladium.palamod.client.gui.RenderHandler";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (!TARGET.equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("guiInstance")) {
                InsnList insns = method.instructions;

                for (AbstractInsnNode insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
                    if (insn.getOpcode() == Opcodes.ALOAD) {
                        if (insn.getNext().getOpcode() == Opcodes.NEW) {
                            if (((TypeInsnNode) insn.getNext()).desc.contains("UIInGameMenu")) {
                                // evil piece of shit
                                AbstractInsnNode n1 = insn;
                                AbstractInsnNode n2 = n1.getNext();
                                AbstractInsnNode n3 = n2.getNext();
                                AbstractInsnNode n4 = n3.getNext();
                                AbstractInsnNode n5 = n4.getNext();
                                AbstractInsnNode n6 = n5.getNext();

                                insns.remove(n6);
                                insns.remove(n5);
                                insns.remove(n4);
                                insns.remove(n3);
                                insns.remove(n2);
                                insns.remove(n1);
                                break;
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
