package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// PalaMod reads com.sk89q.worldguard.protection.flags.DefaultFlag fields directly
// (ItemCaptureStone.func_77648_a line 103 reads DefaultFlag.DENY_SPAWN, and friends).
//
// On a Forge/Crucible server DefaultFlag is loaded by the LaunchClassLoader, but its
// WorldEdit dependency (com.sk89q.minecraft.util.commands.CommandException) lives in
// the WorldEdit Bukkit plugin jar and is not visible there. The first read of any
// DefaultFlag field therefore fails inside its static initializer:
//
//   java.lang.NoClassDefFoundError: com/sk89q/minecraft/util/commands/CommandException
//       at com.sk89q.worldguard.protection.flags.DefaultFlag.<clinit>(DefaultFlag.java:134)
//       at fr.paladium.palamod.modules.hunter.items.ItemCaptureStone.func_77648_a(ItemCaptureStone.java:103)
//
// The exception is thrown while the argument is being evaluated in the caller, so the
// defensive try/catch inside WorldGuardUtils.checkFlag/getStates cannot catch it.
//
// Replacing every DefaultFlag read with null keeps the class from ever being
// initialized. The WorldGuardUtils helpers no longer look at the flag anyway.
public class PalaModDoNotTouchWorldGuardFlags extends BasePatch {

    private static final String FLAG_OWNER = "com/sk89q/worldguard/protection/flags/DefaultFlag";

    private static final Set<String> TARGETS = new HashSet<String>(Arrays.asList(
            "fr.paladium.palamod.modules.paladium.common.events.PlayerHandler",
            "fr.paladium.palamod.modules.hunter.items.ItemCaptureStone",
            "fr.paladium.palamod.modules.hunter.events.EventsManagerServer",
            "fr.paladium.palamod.modules.luckyblock.blocks.BlockBlazeAndSteelFire",
            "fr.paladium.palamod.modules.luckyblock.events.EventsManager",
            "fr.paladium.palamod.modules.back2future.ModEnchantments"
    ));

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        if (!TARGETS.contains(transformedName)) return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        boolean patched = false;
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn.getOpcode() != Opcodes.GETSTATIC || !(insn instanceof FieldInsnNode)) continue;

                FieldInsnNode field = (FieldInsnNode) insn;
                if (!FLAG_OWNER.equals(field.owner)) continue;

                method.instructions.set(insn, new InsnNode(Opcodes.ACONST_NULL));
                patched = true;
            }
        }

        if (!patched) return basicClass;

        System.out.println("[Fspatches] Removed WorldGuard DefaultFlag access in " + transformedName);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
