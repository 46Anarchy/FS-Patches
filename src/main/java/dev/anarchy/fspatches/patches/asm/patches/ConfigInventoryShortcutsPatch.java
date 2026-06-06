package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ConfigInventoryShortcutsPatch extends BasePatch {
    public static final String TARGET = "fr.paladium.palavanillagui.client.config.inventory.ConfigInventoryShortcut";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("<init>") && method.desc.equals("()V")) {

                for (AbstractInsnNode insn : method.instructions.toArray()) {
                }

                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();

                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));

                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));

                insn.add(new IntInsnNode(Opcodes.BIPUSH, 3));
                insn.add(new TypeInsnNode(Opcodes.ANEWARRAY, "fr/paladium/palavanillagui/common/utils/InventoryShortcut"));

                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new InsnNode(Opcodes.ICONST_0));
                insn.add(new TypeInsnNode(Opcodes.NEW, "fr/paladium/palavanillagui/common/utils/InventoryShortcut"));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new LdcInsnNode("https://pictures.paladium-pvp.fr/inv/jobs.png"));
                insn.add(new LdcInsnNode("vanilla.gui.shortcut.jobs.name"));
                insn.add(new LdcInsnNode("/palajobs opengui"));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "fr/paladium/palavanillagui/common/utils/InventoryShortcut", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
                insn.add(new InsnNode(Opcodes.AASTORE));

                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new InsnNode(Opcodes.ICONST_1));
                insn.add(new TypeInsnNode(Opcodes.NEW, "fr/paladium/palavanillagui/common/utils/InventoryShortcut"));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new LdcInsnNode("https://pictures.paladium-pvp.fr/inv/pet.png"));
                insn.add(new LdcInsnNode("vanilla.gui.shortcut.pet.name"));
                insn.add(new LdcInsnNode("/pet"));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "fr/paladium/palavanillagui/common/utils/InventoryShortcut", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
                insn.add(new InsnNode(Opcodes.AASTORE));

                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new InsnNode(Opcodes.ICONST_2));
                insn.add(new TypeInsnNode(Opcodes.NEW, "fr/paladium/palavanillagui/common/utils/InventoryShortcut"));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new LdcInsnNode("https://www.freepnglogos.com/uploads/discord-logo-png/discord-icon-flat-style-available-svg-png-eps-10.png"));
                insn.add(new LdcInsnNode("discord"));
                insn.add(new LdcInsnNode("/discord"));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "fr/paladium/palavanillagui/common/utils/InventoryShortcut", "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", false));
                insn.add(new InsnNode(Opcodes.AASTORE));

                insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Arrays", "asList", "([Ljava/lang/Object;)Ljava/util/List;", false));


                insn.add(new TypeInsnNode(Opcodes.NEW, "java/util/LinkedList"));
                insn.add(new InsnNode(Opcodes.DUP_X1));
                insn.add(new InsnNode(Opcodes.SWAP));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/LinkedList", "<init>", "(Ljava/util/Collection;)V", false));

                insn.add(new FieldInsnNode(Opcodes.PUTFIELD, "fr/paladium/palavanillagui/client/config/inventory/ConfigInventoryShortcut", "shortcuts", "Ljava/util/LinkedList;"));

                insn.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insn.add(new TypeInsnNode(Opcodes.NEW, "java/util/LinkedList"));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/util/LinkedList", "<init>", "()V", false));
                insn.add(new FieldInsnNode(Opcodes.PUTFIELD, "fr/paladium/palavanillagui/client/config/inventory/ConfigInventoryShortcut", "lastUsed", "Ljava/util/LinkedList;"));

                insn.add(new InsnNode(Opcodes.RETURN));

                method.instructions.add(insn);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}