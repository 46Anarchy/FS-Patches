package dev.anarchy.fspatches.patches.mixin;

import com.google.common.collect.Queues;
import cpw.mods.fml.common.Loader;
import fr.paladium.lib.apollon.fontV2.FontObj;
import fr.paladium.lib.apollon.nodes.abstracts.ANode;
import fr.paladium.lib.apollon.ui.AbstractUI;
import fr.paladium.lib.apollon.utils.Color;
import fr.paladium.lib.apollon.utils.Fonts;
import fr.paladium.lib.apollon.utils.GuiUtils;
import fr.paladium.lib.apollon.utils.text.TextAlign;
import fr.paladium.palamod.modules.paladium.client.gui.palamenu.mainmenu.UIMainMenu;
import fr.paladium.palamod.modules.paladium.client.gui.palamenu.mainmenu.node.MainMenuSelectionNode;
import fr.paladium.palamod.modules.paladium.client.gui.palamenu.mainmenu.utils.connector.ServerConnector;
import fr.paladium.palamod.modules.paladium.client.gui.palamenu.mainmenu.utils.connector.ServerSession;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Queue;

@Mixin(UIMainMenu.class)
public class UIMainMenuMixin {

    @Shadow
    private static ResourceLocation SETTINGS_BACKGROUND_TEXTURE;

    @Shadow
    private static ResourceLocation PLAY_BACKGROUND_TEXTURE;

    @Shadow
    private static ResourceLocation NEWS_BACKGROUND_TEXTURE;

    @Shadow
    private ServerSession session;

    @Unique
    private static ResourceLocation LOGO = new ResourceLocation("fspatches", "textures/logo.png");

    @SneakyThrows
    @Inject(method = "<init>()V", at = @At("RETURN"), remap = false)
    private void onInitPatch(CallbackInfo ci) {

        PLAY_BACKGROUND_TEXTURE = new ResourceLocation("fspatches", "textures/background/play.png");
        NEWS_BACKGROUND_TEXTURE = new ResourceLocation("fspatches", "textures/background/news.png");
        SETTINGS_BACKGROUND_TEXTURE = new ResourceLocation("fspatches", "textures/background/settings.png");
        // this shit turn the default icon into an empty png lol
        Field logo = UIMainMenu.class.getDeclaredField("PALADIUM_ICON");
        logo.setAccessible(true);

        Field modifiers = logo.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(logo, logo.getModifiers() & ~Modifier.FINAL);

        logo.set(((UIMainMenu) (Object) this), new ResourceLocation("fspatches", "textures/empty.png"));
    }

    @Inject(method = "postDraw", at = @At("HEAD"), remap = false)
    public void onPostDrawPatch(int mouseX, int mouseY, float ticks, CallbackInfo ci) {
        UIMainMenu self = ((UIMainMenu) (Object) this);
        self.drawSplittedString("46Anarchy by La Collecteuse & Anakoni", (double) self.width(99.0F), (double) self.height(93.0F), Color.WHITE, Fonts.PIXEL_NES.getFont(), 0, 9999, TextAlign.RIGHT);
        GuiUtils.drawImageTransparent((double) self.width(1F), (double) self.height(1F), LOGO, (double) self.width(10F), (double) self.width(10F));
    }

    // this is fucked up on a whole new level, maybe it's even the worst Java I have ever written.
    @SneakyThrows
    @Inject(method = "func_73866_w_", at = @At("TAIL"), remap = false)
    public void onInitEndPatch(CallbackInfo ci) {
        UIMainMenu self = ((UIMainMenu) (Object) this);
        Field nodeField = AbstractUI.class.getDeclaredField("nodes");
        nodeField.setAccessible(true);
        AbstractUI.class.getDeclaredField("nodes").setAccessible(true);
        Queue<ANode> nodes = (Queue<ANode>) nodeField.get((AbstractUI) self);

        nodeField.set((AbstractUI) self, Queues.newConcurrentLinkedQueue());

        boolean isReplayModPresent = Loader.isModLoaded("replaymod");

        nodes.removeIf(anode -> {
            if (anode instanceof MainMenuSelectionNode) {
                MainMenuSelectionNode mnode = (MainMenuSelectionNode) anode;

                try {
                    Field textField = MainMenuSelectionNode.class.getDeclaredField("text");
                    textField.setAccessible(true);
                    String text = textField.get(mnode).toString();

                    if (text.equalsIgnoreCase("BOUTIQUE") && !isReplayModPresent)
                        return true;
                    if (text.equalsIgnoreCase("NEWS") && !isReplayModPresent)
                        return true;

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }

                return false;
            }
            return false;
        });

        nodes.forEach(node -> {
            if (node instanceof MainMenuSelectionNode) {
                MainMenuSelectionNode mnode = (MainMenuSelectionNode) node;

                try {
                    Field textField = MainMenuSelectionNode.class.getDeclaredField("text");
                    textField.setAccessible(true);
                    String text = textField.get(mnode).toString();

                    if (isReplayModPresent && text.equalsIgnoreCase("NEWS")) {
                        mnode.setText("DISCORD");
                        mnode.setCallback((n) -> GuiUtils.openBrowser("https://discord.46anarchy.fr"));
                    } else {
                        self.addNode((new MainMenuSelectionNode((double)self.width(7.08F), (double)self.height(58.739998F), (double)self.width(16.0F), (double)self.height(7.0F), "discord", NEWS_BACKGROUND_TEXTURE, 150, false)).setCallback((n) -> GuiUtils.openBrowser("https://discord.46anarchy.fr")));
                    }

                    if (text.equalsIgnoreCase("BOUTIQUE") && isReplayModPresent) {
                        mnode.setText("REPLAYS");
                        mnode.setCallback((n) -> {
                            try {
                                Object screen = Class
                                        .forName("com.replaymod.replay.gui.screen.GuiReplayViewer")
                                        .getDeclaredConstructors()[0]
                                        .newInstance(Class
                                                .forName("com.replaymod.replay.ReplayModReplay")
                                                .getField("instance")
                                                .get(null));

                                // Lord forgive me for I have sinned.
                                GuiScreen replayScreen = (GuiScreen) screen
                                        .getClass()
                                        .getSuperclass()
                                        .getSuperclass()
                                        .getDeclaredMethod("toMinecraft")
                                        .invoke(screen);

                                if (replayScreen == null)
                                    System.out.println("shiiiiiiiiiiiiiiiit, replaymod screen is null !");

                                Minecraft.getMinecraft().displayGuiScreen(replayScreen);
                            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException |
                                     NoSuchFieldException | InvocationTargetException | NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }

                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        if (Minecraft.getMinecraft().getSession().getUsername().equalsIgnoreCase("AdminAbuseGaming") || Minecraft.getMinecraft().getSession().getUsername().equalsIgnoreCase("Anakoni") ) {
            MainMenuSelectionNode debugNode = new MainMenuSelectionNode
                    ((double) self.width(0), (double) self.height(0),
                            (double) self.width(16.0F), (double) self.height(13.8F),
                            "LOCALHOST", SETTINGS_BACKGROUND_TEXTURE, 350, false);
            debugNode.setCallback((call) -> {
                call.setEnabled(false);
                ((MainMenuSelectionNode) call).setText("Connecting...");
                this.session = ServerConnector.connect("127.0.0.1", 25565, (s) -> {
                    if ("failed".equalsIgnoreCase(s) || "cancelled".equalsIgnoreCase(s)) {
                        call.setEnabled(true);
                        if ("failed".equalsIgnoreCase(s)) {
                            ((MainMenuSelectionNode) call).setText("LOCALHOST");
                        } else {
                            ((MainMenuSelectionNode) call).setText("jouer");
                        }
                    }
                });
            });
            nodes.add(debugNode);
        }

        nodes.forEach(self::addNode);
    }

    @Inject(method = "checkMaintenance", at = @At("HEAD"), cancellable = true, remap = false)
    public void checkMaintenanceBypass(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Redirect(
            method = "postDraw",
            at = @At(
                    value = "INVOKE",
                    target = "Lfr/paladium/palamod/modules/paladium/client/gui/palamenu/mainmenu/UIMainMenu;drawSplittedString(Ljava/lang/String;DDLfr/paladium/lib/apollon/utils/Color;Lfr/paladium/lib/apollon/fontV2/FontObj;IDLfr/paladium/lib/apollon/utils/text/TextAlign;)V",
                    ordinal = 3
            ),
            remap = false
    )
    private void replaceAdventureText(UIMainMenu instance, String text, double x, double y, Color color, FontObj font, int fontSize, double maxWidth, TextAlign align) {
        instance.drawSplittedString("anarchy", x, y, color, font, fontSize, maxWidth, align);
    }
}
