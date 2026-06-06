package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.RETURN;

// patch to remove a now unused dupe detection method in the Storage Drawers mod.
// it works by taking the DrawerBlocks#alert method and replacing it by a return instruction.
public class BlockDrawersDontKickOnInteractions extends BasePatch {

    private static final String TARGET = "com.jaquadro.minecraft.storagedrawers.block.BlockDrawers";
    private static final String TARGET_INTERNAL = "com/jaquadro/minecraft/storagedrawers/block/BlockDrawers";
    private static final String SELF_INTERNAL =
            "dev/anarchy/fspatches/asm/patches/BlockDrawersDontKickOnInteractions";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (basicClass == null)
            return null;

        if (!TARGET.equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("alert")) {
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(RETURN));
                method.instructions.add(insn);
                method.maxStack = 2;
                method.maxLocals = 0;
                patched = true;
            }
        }

        if (!patched)
            return basicClass;

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
