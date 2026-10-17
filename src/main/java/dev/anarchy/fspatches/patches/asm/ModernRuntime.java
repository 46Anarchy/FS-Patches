package dev.anarchy.fspatches.patches.asm;

/**
 * Detects the experimental LWJGL3 / Java 21 runtime (lwjgl3ify + RFB) so the
 * modern-only ASM patches never touch the classic Java 8 path.
 *
 * <p>Detection is class-presence based — no launcher or OS sniffing:
 * <ul>
 *   <li>{@code org.lwjgl.Version} ships with LWJGL3 only (LWJGL2 has
 *       {@code org.lwjgl.Sys}, not {@code Version});</li>
 *   <li>{@code com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader}
 *       is RetroFuturaBootstrap, i.e. lwjgl3ify's launcher.</li>
 * </ul>
 *
 * <p>Both probes use {@code Class.forName(name, false, loader)} (no class
 * initialization, exceptions swallowed) from our own loader then the system
 * loader, and the verdict is cached: the runtime flavor cannot change
 * mid-process.
 */
public final class ModernRuntime {

    private static volatile Boolean modern;

    private ModernRuntime() {}

    /** {@code true} on the lwjgl3ify / Java 21 runtime, {@code false} on classic Java 8. */
    public static boolean isLwjgl3ify() {
        Boolean cached = modern;
        if (cached != null) {
            return cached;
        }
        boolean found = probe("org.lwjgl.Version")
                || probe("com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader");
        modern = found;
        return found;
    }

    private static boolean probe(String className) {
        try {
            Class.forName(className, false, ModernRuntime.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
        }
        try {
            Class.forName(className, false, ClassLoader.getSystemClassLoader());
            return true;
        } catch (Throwable ignored) {
        }
        return false;
    }
}
