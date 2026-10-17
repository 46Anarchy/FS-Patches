package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

// WorldGuard only exists as a Bukkit plugin here. On a Forge/Crucible server the
// Pala* mods read com.sk89q.worldguard.protection.flags.DefaultFlag.* directly,
// which dies with NoClassDefFoundError because the Forge LaunchClassLoader cannot
// resolve WorldEdit's com.sk89q.minecraft.util.commands classes.
//
// The callers no longer read DefaultFlag at all (see PalaModDoNotTouchWorldGuardFlags),
// so these helpers must not dereference the flag they are handed either: the StateFlag
// overload dereferences it before its try/catch, so a null flag would turn the missing
// class into an uncaught NullPointerException.
public class WorldGuardDoNotCheckFlags extends BasePatch {

    public static final String TARGET = "fr.paladium.palaforgeutils.lib.bukkit.WorldGuardUtils";

    private static final String CHECK_FLAG_STATE =
            "(Lnet/minecraft/world/World;DDDLcom/sk89q/worldguard/protection/flags/StateFlag;Z)Z";
    private static final String CHECK_FLAG_SET =
            "(Lnet/minecraft/world/World;DDDLcom/sk89q/worldguard/protection/flags/SetFlag;Ljava/lang/Object;)Z";
    private static final String GET_STATES =
            "(Lnet/minecraft/world/World;DDDLcom/sk89q/worldguard/protection/flags/SetFlag;)Ljava/util/Set;";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGET.equals(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (method.name.equals("checkFlag") && method.desc.equals(CHECK_FLAG_STATE)) {
                // No WorldGuard -> keep whatever default the caller asked for (always "allowed" here).
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new VarInsnNode(Opcodes.ILOAD, 8));
                insn.add(new InsnNode(Opcodes.IRETURN));
                method.instructions.add(insn);
                patched = true;
            } else if (method.name.equals("checkFlag") && method.desc.equals(CHECK_FLAG_SET)) {
                // No WorldGuard -> nothing is ever denied.
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(Opcodes.ICONST_1));
                insn.add(new InsnNode(Opcodes.IRETURN));
                method.instructions.add(insn);
                patched = true;
            } else if (method.name.equals("getStates") && method.desc.equals(GET_STATES)) {
                // No WorldGuard -> no flags are set anywhere.
                method.instructions.clear();
                method.tryCatchBlocks.clear();

                InsnList insn = new InsnList();
                insn.add(new InsnNode(Opcodes.ACONST_NULL));
                insn.add(new InsnNode(Opcodes.ARETURN));
                method.instructions.add(insn);
                patched = true;
            }
        }

        if (!patched) return basicClass;

        System.out.println("[Fspatches] WorldGuardUtils flag checks disabled");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
