package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import fr.paladium.helios.module.tab.ModuleTab;
import fr.paladium.helios.module.tab.TabPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerInfo;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

// just a quick patch to change one or two instructions in the tab system.
// this is needed to customize it how we want
public class UITabOverlayTweaks extends BasePatch {

    private static final String TARGET = "fr.paladium.helios.module.tab.UITabOverlay";
    private static final String DRAW_DISCORD_CALL = "dev/anarchy/fspatches/patches/utils/DrawUtils";

    public static final String customEnvName = "Anarchie Moddée";

    public static void refreshPlayerList() {
        ModuleTab.getInstance().getPlayers().clear();

        for (GuiPlayerInfo info : (List<GuiPlayerInfo>)Minecraft.getMinecraft().getNetHandler().playerInfoList) {
            ModuleTab.getInstance().getPlayers().add(new TabPlayer(info.name, null));
        }
    }


    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (!TARGET.equals(transformedName))
            return basicClass;

        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);

        for (MethodNode method : node.methods) {
            if (method.name.equals("preDraw")) {
                InsnList instructions = method.instructions;
                List<AbstractInsnNode> toInjectAfter = new ArrayList<>();

                InsnList refreshPlayerList = new InsnList();

                refreshPlayerList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "dev/anarchy/fspatches/patches/asm/patches/UITabOverlayTweaks", "refreshPlayerList", "()V", false));
                method.instructions.insertBefore(method.instructions.getFirst(), refreshPlayerList);

                for (AbstractInsnNode insn = instructions.getFirst(); insn != null; insn = insn.getNext()) {

                    if (!(insn instanceof LdcInsnNode)) continue;
                    LdcInsnNode lnode = (LdcInsnNode) insn;
                    Object cst = lnode.cst;

                    // this is fucking stupid.
                    // instructions#set cause issues with the loop.
                    // so we're fucked.
                    if ("textures/widgets/tab/logo.png".equals(cst))
                        ((LdcInsnNode) insn).cst = "textures/widgets/tab/logo_custom.png";

                    if (cst instanceof Double && Math.abs((Double) cst - 91.99440000000001) < 0.001) {
                        AbstractInsnNode prev = lnode.getPrevious();
                        if (prev instanceof LdcInsnNode && Math.abs((Double)((LdcInsnNode) prev).cst - 330.0) < 0.001) {
                            lnode.cst = 181.0;
                        }
                    }

                    if (cst instanceof Double && Math.abs((Double) cst - 84.45599999999999) < 0.001) {
                        AbstractInsnNode next = lnode.getNext();
                        if (next instanceof LdcInsnNode && Math.abs((Double)((LdcInsnNode) next).cst - 330.0) < 0.001) {
                            lnode.cst = 44.456;
                        }
                    }

                    if (cst.equals("SERVEUR")) {
                        AbstractInsnNode next = insn.getNext();
                        while (next != null
                                && !(next instanceof MethodInsnNode
                                && ((MethodInsnNode) next).name.equals("drawText"))) {
                            next = next.getNext();
                        }
                        if (next instanceof MethodInsnNode)
                            toInjectAfter.add(next);
                    }
                }

                for (AbstractInsnNode target : toInjectAfter) {
                    InsnList inject = new InsnList();
                    inject.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            "dev/anarchy/fspatches/patches/utils/DrawUtils",
                            "drawDiscord",
                            "()V",
                            false
                    ));
                    instructions.insert(target, inject);
                }
            }
            if (method.name.equals("setEnvName")) {
                method.instructions.clear();
                InsnList insns = new InsnList();

                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insns.add(new LdcInsnNode("Anarchie Moddée"));
                insns.add(new FieldInsnNode(
                        Opcodes.PUTFIELD,
                        "fr/paladium/helios/module/tab/UITabOverlay",
                        "envName",
                        "Ljava/lang/String;"
                ));

                insns.add(new InsnNode(Opcodes.RETURN));
                method.instructions.add(insns);
                method.maxStack = 2;
                method.maxLocals = 2;
            }
        }
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(writer);
        return writer.toByteArray();
    }
}
