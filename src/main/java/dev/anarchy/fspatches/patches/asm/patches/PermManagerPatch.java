package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class PermManagerPatch extends BasePatch {

    public static final String TARGET = "fr.paladium.factions.server.permissions.PermManager";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("getPermissionProvider")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();

                String owner = "fr/paladium/factions/server/permissions/PermManager";
                String providerDesc = "Lfr/paladium/factions/server/permissions/IPermissionProvider;";
                String defaultProvDesc = "dev/anarchy/fspatches/content/misc/FsPermissionProvider";
                String loggerOwner = "fr/paladium/factions/FactionLogger";

                insn.add(new FieldInsnNode(Opcodes.GETSTATIC, owner, "provider", providerDesc));
                LabelNode notNull = new LabelNode();
                insn.add(new JumpInsnNode(Opcodes.IFNONNULL, notNull));

                insn.add(new TypeInsnNode(Opcodes.NEW, defaultProvDesc));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, defaultProvDesc, "<init>", "()V", false));
                insn.add(new FieldInsnNode(Opcodes.PUTSTATIC, owner, "provider", providerDesc));

                insn.add(new TypeInsnNode(Opcodes.NEW, "java/lang/StringBuilder"));
                insn.add(new InsnNode(Opcodes.DUP));
                insn.add(new LdcInsnNode("Loaded permission provider: "));
                insn.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
                insn.add(new FieldInsnNode(Opcodes.GETSTATIC, owner, "provider", providerDesc));
                insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
                insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;", false));
                insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
                insn.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
                insn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, loggerOwner, "info", "(Ljava/lang/String;)V", false));

                insn.add(notNull);

                insn.add(new FieldInsnNode(Opcodes.GETSTATIC, owner, "provider", providerDesc));
                insn.add(new InsnNode(Opcodes.ARETURN));

                method.instructions.add(insn);
            }
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
