package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import dev.anarchy.fspatches.patches.asm.ModernRuntime;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.awt.Desktop;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class DesktopBrowseSysOpenUrlPatch extends BasePatch {

    private static final String SELF_INTERNAL = "dev/anarchy/fspatches/patches/asm/patches/DesktopBrowseSysOpenUrlPatch";
    private static final String LOCKED_NODE = "fr.paladium.palavanillagui.client.ui.chest.utils.LockedNode";
    private static final String PALAMOD_UTILS = "fr.paladium.palamod.client.utils.Utils";
    private static final String PALAMOD_UTILS_INTERNAL = "fr/paladium/palamod/client/utils/Utils";

    public static void browseShim(Desktop ignored, URI uri) {
        String url;
        try {
            url = String.valueOf(uri);
        } catch (Throwable t) {
            return;
        }
        try {
            Object opened = Class.forName("org.lwjgl.Sys").getMethod("openURL", String.class).invoke(null, url);
            if (Boolean.TRUE.equals(opened)) {
                return;
            }
        } catch (Throwable ignored2) {
        }
        try {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        // Java 8 players keep the original classes untouched (see ModernRuntime).
        if (!ModernRuntime.isLwjgl3ify()) return basicClass;
        boolean palamodUtils = PALAMOD_UTILS.equals(transformedName);
        if (!LOCKED_NODE.equals(transformedName) && !palamodUtils) return basicClass;
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (palamodUtils && !method.name.equals("openBrowser")) continue;
            List<MethodInsnNode> desktops = new ArrayList<>();
            boolean hasBrowse = false;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (call.getOpcode() == INVOKEVIRTUAL && call.owner.equals("java/awt/Desktop") && call.name.equals("browse")) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF_INTERNAL, "browseShim", "(Ljava/awt/Desktop;Ljava/net/URI;)V", false));
                    hasBrowse = true;
                } else if (call.getOpcode() == INVOKESTATIC && call.owner.equals("java/awt/Desktop") && call.name.equals("getDesktop")) {
                    desktops.add(call);
                } else if (palamodUtils && call.getOpcode() == INVOKESTATIC && call.owner.equals("java/awt/Desktop") && call.name.equals("isDesktopSupported") && "()Z".equals(call.desc)) {
                    method.instructions.set(call, new InsnNode(ICONST_1));
                } else if (palamodUtils && call.getOpcode() == INVOKESTATIC && call.owner.equals(PALAMOD_UTILS_INTERNAL) && "()Z".equals(call.desc) && (call.name.equals("isWindows") || call.name.equals("isMac"))) {
                    method.instructions.set(call, new InsnNode(ICONST_1));
                }
            }
            if (!hasBrowse) continue;
            for (MethodInsnNode desktop : desktops) method.instructions.set(desktop, new InsnNode(ACONST_NULL));
            patched = true;
        }
        if (!patched) return basicClass;
        System.out.println("[FSASM] DesktopBrowseSysOpenUrlPatch applied to " + transformedName);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
