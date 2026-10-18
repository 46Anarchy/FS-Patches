package dev.anarchy.fspatches.patches.asm.patches;

import dev.anarchy.fspatches.patches.asm.BasePatch;
import dev.anarchy.fspatches.patches.asm.ModernRuntime;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.objectweb.asm.Opcodes.*;

/**
 * ZephyrUI supprime ses textures et framebuffers depuis des {@code finalize()},
 * donc sur le thread Finalizer sans contexte GL courant. LWJGL2 se contentait
 * d'une exception avalée ; LWJGL3 (runtime expérimental) fait avorter la JVM :
 * {@code FATAL ERROR in native method ... No context is current or a function
 * that is not available in the current context was called} (crash client du
 * 18/10, {@code ResourceData.finalize} → {@code clear} → {@code glDeleteTextures}).
 *
 * <p>Le chemin finalizer des trois classes concernées est remplacé par une mise
 * en file ; la suppression est exécutée au tick client suivant, contexte
 * courant, donc rien ne fuit et la JVM n'avorte plus :
 * <ul>
 *   <li>{@code ResourceData.finalize()} → {@code clear()} ;</li>
 *   <li>{@code FrameBuffer.finalize()} → {@code delete()} ;</li>
 *   <li>{@code AsyncResourceLocation.finalize()} →
 *       {@code TextureUtil.deleteTexture(int)}.</li>
 * </ul>
 *
 * <p>Les appels explicites (thread de rendu) ne sont pas touchés. Comme les
 * autres correctifs du runtime moderne, tout est conditionné à
 * {@link ModernRuntime#isLwjgl3ify()} : sur le classique Java 8 les classes
 * sont rendues telles quelles.
 */
public class ZephyrGlFinalizeSafePatch extends BasePatch {

    private static final String SELF =
            "dev/anarchy/fspatches/patches/asm/patches/ZephyrGlFinalizeSafePatch";

    private static final String RESOURCE_DATA =
            "fr.paladium.zephyrui.lib.resource.dto.ResourceData";
    private static final String FRAME_BUFFER =
            "fr.paladium.zephyrui.lib.opengl.framebuffer.FrameBuffer";
    private static final String ASYNC_RESOURCE_LOCATION =
            "fr.paladium.zephyrui.lib.resource.vanilla.AsyncResourceLocation";
    // Noms internes (forme « / ») pour comparer `MethodInsnNode.owner`.
    private static final String RESOURCE_DATA_INTERNAL =
            "fr/paladium/zephyrui/lib/resource/dto/ResourceData";
    private static final String FRAME_BUFFER_INTERNAL =
            "fr/paladium/zephyrui/lib/opengl/framebuffer/FrameBuffer";
    private static final String TEXTURE_UTIL =
            "net/minecraft/client/renderer/texture/TextureUtil";

    /** Borne des files : au-delà on abandonne la suppression (jamais d'OOM). */
    private static final int CAP = 8192;

    private static final Queue<Object> PENDING_CLEAR = new ConcurrentLinkedQueue<Object>();
    private static final Queue<Object> PENDING_DELETE = new ConcurrentLinkedQueue<Object>();
    private static final Queue<Integer> PENDING_TEXTURE = new ConcurrentLinkedQueue<Integer>();

    private static Method glCapabilities;
    private static boolean glCapabilitiesResolved;
    private static boolean loggedSkip;
    private static boolean loggedError;

    /** Contexte GL courant sur CE thread ? ({@code false} hors LWJGL3 aussi). */
    public static boolean hasContext() {
        if (!glCapabilitiesResolved) {
            glCapabilitiesResolved = true;
            try {
                glCapabilities =
                        Class.forName("org.lwjgl.opengl.GL").getMethod("getCapabilities");
            } catch (Throwable ignored) {
                glCapabilities = null;
            }
        }
        if (glCapabilities == null) {
            return false;
        }
        try {
            return glCapabilities.invoke(null) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** Remplace {@code ResourceData.clear()} dans {@code finalize()}. */
    public static void deferClear(fr.paladium.zephyrui.lib.resource.dto.ResourceData data) {
        if (data == null) {
            return;
        }
        if (hasContext()) {
            try {
                data.clear();
            } catch (Throwable t) {
                logError("ResourceData.clear", t);
            }
            return;
        }
        if (PENDING_CLEAR.size() >= CAP) {
            logSkip();
            return;
        }
        PENDING_CLEAR.add(data);
    }

    /** Remplace {@code FrameBuffer.delete()} dans {@code finalize()}. */
    public static void deferDelete(fr.paladium.zephyrui.lib.opengl.framebuffer.FrameBuffer framebuffer) {
        if (framebuffer == null) {
            return;
        }
        if (hasContext()) {
            try {
                framebuffer.delete();
            } catch (Throwable t) {
                logError("FrameBuffer.delete", t);
            }
            return;
        }
        if (PENDING_DELETE.size() >= CAP) {
            logSkip();
            return;
        }
        PENDING_DELETE.add(framebuffer);
    }

    /** Remplace {@code TextureUtil.deleteTexture(int)} dans {@code finalize()}. */
    public static void deferTextureDelete(int textureId) {
        if (textureId == -1) {
            return;
        }
        if (hasContext()) {
            try {
                TextureUtil.deleteTexture(textureId);
            } catch (Throwable t) {
                logError("TextureUtil.deleteTexture", t);
            }
            return;
        }
        if (PENDING_TEXTURE.size() >= CAP) {
            logSkip();
            return;
        }
        PENDING_TEXTURE.add(Integer.valueOf(textureId));
    }

    /**
     * Vide les files — appelé au tick client ({@code ZephyrGlDrainHandler}),
     * donc avec un contexte GL courant. Sans contexte (ou hors LWJGL3) on ne
     * touche à rien : les files sont vides sur le classique.
     */
    public static void drain() {
        if (PENDING_CLEAR.isEmpty() && PENDING_DELETE.isEmpty() && PENDING_TEXTURE.isEmpty()) {
            return;
        }
        if (!hasContext()) {
            return;
        }
        Object pending;
        while ((pending = PENDING_CLEAR.poll()) != null) {
            try {
                ((fr.paladium.zephyrui.lib.resource.dto.ResourceData) pending).clear();
            } catch (Throwable t) {
                logError("drain clear", t);
            }
        }
        while ((pending = PENDING_DELETE.poll()) != null) {
            try {
                ((fr.paladium.zephyrui.lib.opengl.framebuffer.FrameBuffer) pending).delete();
            } catch (Throwable t) {
                logError("drain delete", t);
            }
        }
        Integer textureId;
        while ((textureId = PENDING_TEXTURE.poll()) != null) {
            try {
                TextureUtil.deleteTexture(textureId.intValue());
            } catch (Throwable t) {
                logError("drain texture", t);
            }
        }
    }

    private static void logSkip() {
        if (loggedSkip) {
            return;
        }
        loggedSkip = true;
        System.out.println("[FSASM] ZephyrGlFinalizeSafePatch : file pleine ("
                + CAP + "), suppressions GL abandonnées jusqu'au prochain vidage.");
    }

    private static void logError(String what, Throwable t) {
        if (loggedError) {
            return;
        }
        loggedError = true;
        System.out.println("[FSASM] ZephyrGlFinalizeSafePatch : échec " + what + " — " + t);
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) return null;
        // Java 8 : comportement LWJGL2 d'origine (voir ModernRuntime).
        if (!ModernRuntime.isLwjgl3ify()) return basicClass;
        if (!RESOURCE_DATA.equals(transformedName)
                && !FRAME_BUFFER.equals(transformedName)
                && !ASYNC_RESOURCE_LOCATION.equals(transformedName)) {
            return basicClass;
        }
        ClassNode node = new ClassNode();
        new ClassReader(basicClass).accept(node, 0);
        boolean patched = false;
        for (MethodNode method : node.methods) {
            if (!"finalize".equals(method.name) || !"()V".equals(method.desc)) continue;
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (!(insn instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) insn;
                if (RESOURCE_DATA.equals(transformedName)
                        && call.getOpcode() == INVOKEVIRTUAL
                        && RESOURCE_DATA_INTERNAL.equals(call.owner)
                        && "clear".equals(call.name)
                        && "()V".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF,
                            "deferClear",
                            "(Lfr/paladium/zephyrui/lib/resource/dto/ResourceData;)V", false));
                    patched = true;
                } else if (FRAME_BUFFER.equals(transformedName)
                        && call.getOpcode() == INVOKEVIRTUAL
                        && FRAME_BUFFER_INTERNAL.equals(call.owner)
                        && "delete".equals(call.name)
                        && "()V".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF,
                            "deferDelete",
                            "(Lfr/paladium/zephyrui/lib/opengl/framebuffer/FrameBuffer;)V", false));
                    patched = true;
                } else if (ASYNC_RESOURCE_LOCATION.equals(transformedName)
                        && call.getOpcode() == INVOKESTATIC
                        && TEXTURE_UTIL.equals(call.owner)
                        && "func_147942_a".equals(call.name)
                        && "(I)V".equals(call.desc)) {
                    method.instructions.set(call, new MethodInsnNode(INVOKESTATIC, SELF,
                            "deferTextureDelete", "(I)V", false));
                    patched = true;
                }
            }
        }
        if (!patched) return basicClass;
        System.out.println("[FSASM] ZephyrGlFinalizeSafePatch applied to " + transformedName);
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }
}
