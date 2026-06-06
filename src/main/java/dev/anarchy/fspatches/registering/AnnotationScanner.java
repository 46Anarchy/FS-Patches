package dev.anarchy.fspatches.registering;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import dev.anarchy.fspatches.registering.annotation.RegisterBlock;
import dev.anarchy.fspatches.registering.annotation.RegisterItem;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.annotation.Annotation;
import java.util.*;

public class AnnotationScanner {

    private static final Map<String, Item>  ITEMS  = new LinkedHashMap<>();
    private static final Map<String, Block> BLOCKS = new LinkedHashMap<>();
    private static final Map<String, Class<? extends TileEntity>> TILE_ENTITY = new LinkedHashMap<>();

    public static void scanItems() {
        List<Class<?>> classes = findAnnotatedClasses(
                RegisterItem.class, "dev.anarchy.fspatches.content.items"
        );
        for (Class<?> clazz : classes) {
            if (!checkMod(clazz)) continue;
            RegisterItem meta = clazz.getAnnotation(RegisterItem.class);
            tryRegisterItem(clazz, meta.value());
        }
    }

    public static void scanBlocks() {
        List<Class<?>> classes = findAnnotatedClasses(
                RegisterBlock.class, "dev.anarchy.fspatches.content.blocks"
        );
        for (Class<?> clazz : classes) {
            if (!checkMod(clazz)) continue;
            RegisterBlock meta = clazz.getAnnotation(RegisterBlock.class);
            tryRegisterBlock(clazz, meta.value(), meta.itemBlock());
        }
    }

    public static void scanAll() {
        scanItems();
        scanBlocks();
    }

    private static List<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation, String packageFilter) {
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .forPackages(packageFilter)
                        .setScanners(new TypeAnnotationsScanner(), new SubTypesScanner(false))
        );
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(annotation);
        return new ArrayList<>(annotated);
    }

    public static void tryRegisterItem(Class<?> clazz, String name) {
        try {
            Item item = (Item) clazz.newInstance();
            GameRegistry.registerItem(item, name);
            ITEMS.put(name, item);
            System.out.println("[FSPatches] Item enregistré : " + name);
        } catch (Exception e) {
            System.out.println("[FSPatches] Echec item : " + name);
            e.printStackTrace();
        }
    }

    public static void tryRegisterBlock(Class<?> clazz, String name, Class<? extends ItemBlock> itemBlockClass) {
        try {
            Block block = (Block) clazz.newInstance();
            GameRegistry.registerBlock(block, itemBlockClass, name);
            BLOCKS.put(name, block);
            System.out.println("[FSPatches] Block enregistré : " + name);
        } catch (Exception e) {
            System.out.println("[FSPatches] Echec block : " + name);
            e.printStackTrace();
        }
    }

    public static void tryRegisterTileEntity(Class<? extends TileEntity> clazz, String name) {
        try {
            GameRegistry.registerTileEntity(clazz, name);
            TILE_ENTITY.put(name, clazz);
            System.out.println("[FSPatches] Tile enregistré : " + name);
        } catch (Exception e) {
            System.out.println("[FSPatches] Echec Tile : " + name);
            e.printStackTrace();
        }
    }

    private static boolean checkMod(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(RequiresMod.class)) return true;
        String modId = clazz.getAnnotation(RequiresMod.class).value();
        if (Loader.isModLoaded(modId)) return true;
        System.out.println("[FSPatches] Ignoré (mod absent: " + modId + ") : "
                + clazz.getSimpleName());
        return false;
    }

    public static Item  getItem(String name)  { return ITEMS.get(name);  }
    public static Block getBlock(String name) { return BLOCKS.get(name); }

    public static boolean isItemAvailable(String name)  { return ITEMS.containsKey(name);  }
    public static boolean isBlockAvailable(String name) { return BLOCKS.containsKey(name); }
}