package dev.anarchy.fspatches;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.relauncher.Side;
import dev.anarchy.fspatches.content.command.DiscordCommand;
import dev.anarchy.fspatches.content.luckyblocks.EventRegistry;
import dev.anarchy.fspatches.patches.asm.FSAsm;
import dev.anarchy.fspatches.patches.utils.JobCustomizer;
import dev.anarchy.fspatches.proxies.CommonProxy;
import dev.anarchy.fspatches.registering.AnnotationScanner;
import dev.anarchy.fspatches.updater.CDNSTATUS;
import dev.anarchy.fspatches.updater.UpdaterTickHandler;
import fr.paladium.palaforgeutils.lib.command.annotated.registry.CommandRegistry;


@Mod(modid = Fspatches.MODID, version = Fspatches.VERSION)
public class Fspatches {
    public static final String MODID = "fspatches";
    public static final String VERSION = "@VERSION@";

    @SidedProxy(clientSide = "dev.anarchy.fspatches.proxies.ClientProxy", serverSide = "dev.anarchy.fspatches.proxies.ServerProxy")
    public static CommonProxy clientProxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        System.out.println("CTRLF FSPatches Version " + Fspatches.VERSION);
        AnnotationScanner.scanAll();
        clientProxy.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded("palamod")) {
            EventRegistry.init();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        if (FSAsm.status != CDNSTATUS.OK) {
            System.out.println("[Fspatches] FSPatches updating mods!]");
            if (event.getSide() == Side.CLIENT && !System.getProperties().containsKey("bypass-cdn-need"))
                cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(new UpdaterTickHandler());
        }

        if (FSAsm.status == CDNSTATUS.CDN_FAILURE) {
            System.out.println("[FSPatches] CDN unavailable :(");
        }
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        JobCustomizer.clearAllBlacklists();
        CommandRegistry.register(new DiscordCommand());
    }

}
