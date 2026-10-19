package dev.anarchy.fspatches.patches.asm;

import java.lang.reflect.Method;

import net.minecraft.launchwrapper.IClassTransformer;

/**
 * Base des patchs ASM.
 *
 * <p>Héberge la détection du runtime moderne : elle DOIT vivre dans une classe
 * déjà chargée quand un patch s'exécute. {@code PatchDispatcher.init()}
 * instancie les patchs pendant un chargement de classe, donc les transformations
 * s'exécutent de façon ré-entrante sur les classes du paquet lui-même : le
 * premier appel à une classe tierce (une sonde séparée type {@code ModernRuntime})
 * déclenchait un chargement de classe imbriqué →
 * {@code ClassCircularityError} au boot (constaté le 18/10 avec un 4ᵉ patch).
 * {@code BasePatch} est chargée par définition (tous les patchs en héritent).
 */
public abstract class BasePatch implements IClassTransformer {

    private static Boolean lwjgl3ify;
    private static boolean probing;

    /**
     * {@code true} sur le runtime lwjgl3ify/Java 21 (RFB), {@code false} sur le
     * classique Java 8. Sonde de présence de classe, sans initialisation, une
     * seule fois par session.
     */
    protected static boolean lwjgl3ifyRuntime() {
        Boolean cached = lwjgl3ify;
        if (cached != null) {
            return cached;
        }
        // Appel imbriqué (une sonde déclenche un chargement de classe, donc des
        // transformations ré-entrantes) : ne rien décider et surtout ne pas
        // mettre en cache un verdict partiel.
        if (probing) {
            return false;
        }
        probing = true;
        try {
            boolean modern = probe("org.lwjgl.Version")
                    || probe("com.gtnewhorizons.retrofuturabootstrap.RfbSystemClassLoader");
            lwjgl3ify = modern;
            return modern;
        } finally {
            probing = false;
        }
    }

    private static boolean probe(String className) {
        try {
            Class.forName(className, false, BasePatch.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Contexte GL courant sur CE thread ? ({@code false} si LWJGL3 est absent).
     * Même règle que la sonde ci-dessus : la classe vit dans un patch déjà
     * chargé pour éviter tout chargement imbriqué pendant une transformation.
     */
    private static Method glCapabilities;
    private static boolean glCapabilitiesResolved;

    protected static boolean glContextPresent() {
        if (!glCapabilitiesResolved) {
            glCapabilitiesResolved = true;
            try {
                glCapabilities = Class.forName("org.lwjgl.opengl.GL").getMethod("getCapabilities");
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
}
