package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ExtendedAchievementPlayerDataPatch extends BasePatch {

    public static final String TARGET =
            "fr.paladium.achievement.core.data.ExtendedAchievementPlayerData";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, ClassReader.EXPAND_FRAMES);

        for (MethodNode method : classNode.methods) {
            if (!method.name.equals("incrementStats")) continue;

            LabelNode tryStart   = new LabelNode();
            LabelNode tryEnd     = new LabelNode();
            LabelNode catchStart = new LabelNode();

            method.instructions.insert(tryStart);

            InsnList suffix = new InsnList();
            suffix.add(tryEnd);
            suffix.add(new InsnNode(Opcodes.RETURN));
            suffix.add(catchStart);
            suffix.add(new InsnNode(Opcodes.POP));
            suffix.add(new InsnNode(Opcodes.RETURN));

            method.instructions.add(suffix);

            method.tryCatchBlocks.add(new TryCatchBlockNode(
                    tryStart,
                    tryEnd,
                    catchStart,
                    "java/lang/NullPointerException"
            ));

        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}