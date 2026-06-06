package dev.anarchy.fspatches.updater;

import dev.anarchy.fspatches.patches.asm.FSAsm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class UpdaterScreen extends GuiScreen {

    private int state = 0;
    private DownloadCouldntCompleteException lastException;
    private Thread downloadThread;
    private ISound currentMusic;

    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("fspatches", "textures/background/settings.png");

    @Override
    public void initGui() {
        this.buttonList.clear();

        int centerX = this.width / 2;
        int y = this.height / 2 + 20;

        if (FSAsm.status == CDNSTATUS.CDN_FAILURE)
            state = -1;

        if (state == 0) {
            this.buttonList.add(new GuiButton(0, centerX - 102, y, 98, 20, "Telecharger"));
            this.buttonList.add(new GuiButton(1, centerX + 4, y, 98, 20, "Quitter"));
        } else if (state == 2) {
            this.buttonList.add(new GuiButton(1, centerX - 100, y, 200, 20, "Quitter le jeu"));
        } else if (state == 3) {
            this.buttonList.add(new GuiButton(1, centerX - 100, y, 200, 20, "Quitter le jeu"));
        }

        if (this.currentMusic == null || !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.currentMusic)) {
            this.currentMusic = PositionedSoundRecord.func_147673_a(new ResourceLocation("fspatches", "music/bg"));
            Minecraft.getMinecraft().getSoundHandler().stopSounds();
            Minecraft.getMinecraft().getSoundHandler().playSound(this.currentMusic);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();

        blockVanillaMusic();

        if (this.currentMusic != null && !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.currentMusic))
            Minecraft.getMinecraft().getSoundHandler().playSound(this.currentMusic);
    }

    private void blockVanillaMusic() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            for (Field field : mc.getClass().getDeclaredFields()) {
                if (field.getType().getName().contains("MusicTicker")) {
                    field.setAccessible(true);
                    Object ticker = field.get(mc);

                    if (ticker != null) {
                        for (Field tickerField : ticker.getClass().getDeclaredFields()) {
                            if (tickerField.getType() == int.class) {
                                tickerField.setAccessible(true);
                                tickerField.setInt(ticker, Integer.MAX_VALUE);
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            state = 1;
            this.buttonList.clear();
            downloadThread = new Thread(() -> {
                try {
                    File manifestFile = new File(".cdn_new.json");
                    List<FileIntegrity.DownloadFile> files =
                            FileIntegrity.buildDownloadList(manifestFile, new File("./"));
                    FileIntegrity.downloadFiles(files);
                    File oldJson = new File(".cdn_last.json");
                    File newJson = new File(".cdn_new.json");
                    try {
                        Files.move(newJson.toPath(), oldJson.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("[Fspatches] Failed to replace cdn json: " + e.getMessage());
                    }
                    state = 2;
                    initGui();
                } catch (DownloadCouldntCompleteException e) {
                    lastException = e;
                    state = 3;
                    initGui();
                }
            });
            downloadThread.setDaemon(true);
            downloadThread.start();
        }

        if (button.id == 1)
            Minecraft.getMinecraft().shutdown();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(BACKGROUND);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        float imgRatio = 1920.0F / 1080.0F;
        float screenRatio = (float) this.width / (float) this.height;

        double drawW, drawH, drawX, drawY;
        if (screenRatio >= imgRatio) {
            drawW = this.width;
            drawH = this.width / imgRatio;
            drawX = 0;
            drawY = (this.height - drawH) / 2.0;
        } else {
            drawH = this.height;
            drawW = this.height * imgRatio;
            drawX = (this.width - drawW) / 2.0;
            drawY = 0;
        }

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertexWithUV(drawX,         drawY + drawH, 0, 0, 1);
        tess.addVertexWithUV(drawX + drawW, drawY + drawH, 0, 1, 1);
        tess.addVertexWithUV(drawX + drawW, drawY,         0, 1, 0);
        tess.addVertexWithUV(drawX,         drawY,         0, 0, 0);
        tess.draw();

        Gui.drawRect(0, 0, this.width, this.height, 0xCC111111);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        drawCenteredString(this.fontRendererObj, "FSPatches", centerX, centerY - 110, 0xFFFFFF);

        if (state == -1) {
            drawCenteredString(fontRendererObj, "Le CDN de Paladium est indisponible.", centerX, centerY - 70, 0xFF5555);
            drawCenteredString(fontRendererObj, "C'est un probleme de Paladium, merci d'attendre.", centerX, centerY - 55, 0x777777);
        } else if (state == 0) {
            drawCenteredString(fontRendererObj, "Une mise a jour du modpack est necessaire.", centerX, centerY - 70, 0xAAAAAA);
            drawCenteredString(fontRendererObj, "FSPatches depend du modpack Paladium.", centerX, centerY - 55, 0x555555);
            drawCenteredString(fontRendererObj, "Les fichiers necessaires doivent etre telecharges.", centerX, centerY - 42, 0x555555);
            drawCenteredString(fontRendererObj, "Le jeu devra etre redemarre apres l'installation.", centerX, centerY - 29, 0xCC4444);
        } else if (state == 1) {
            drawCenteredString(fontRendererObj, "Telechargement du modpack...", centerX, centerY - 70, 0xAAAAAA);
            drawCenteredString(fontRendererObj, "Veuillez patienter.", centerX, centerY - 55, 0x555555);

            GL11.glPushMatrix();
            GL11.glScaled(0.75, 0.75, 1);
            drawDownloadList();
            GL11.glPopMatrix();
        } else if (state == 2) {
            drawCenteredString(fontRendererObj, "Installation terminee.", centerX, centerY - 70, 0x55FF55);
            drawCenteredString(fontRendererObj, "Le jeu doit maintenant etre redemarre.", centerX, centerY - 55, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "Veuillez quitter Minecraft.", centerX, centerY - 42, 0x777777);
        } else if (state == 3) {
            drawCenteredString(fontRendererObj, "Le telechargement a echoue.", centerX, centerY - 70, 0xFF5555);
            if (lastException != null)
                drawCenteredString(fontRendererObj, "Fichier : " + lastException.getFileName(), centerX, centerY - 55, 0xFFFFFF);
            drawCenteredString(fontRendererObj, "Veuillez verifier votre connexion internet.", centerX, centerY - 40, 0x777777);
            drawCenteredString(fontRendererObj, "Puis relancez le jeu.", centerX, centerY - 27, 0x777777);
        }

        int barAreaY = this.height - 55;
        Gui.drawRect(0, barAreaY, this.width, this.height, 0xDD0D0D0D);
        Gui.drawRect(0, barAreaY, this.width, barAreaY + 1, 0xFF222222);

        if (state == 1) {
            int total = FileIntegrity.getDownloadedFileCount() + FileIntegrity.getDownloadingFileCount() + FileIntegrity.getRemainingFileCount();
            if (total <= 0) total = 1;

            int barX = 20;
            int barWidth = this.width - 40;
            int barHeight = 14;
            int barInnerY = barAreaY + 14;

            float downloadedRatio = (float) FileIntegrity.getDownloadedFileCount() / total;
            float downloadingRatio = (float) FileIntegrity.getDownloadingFileCount() / total;
            int downloadedW = (int) (barWidth * downloadedRatio);
            int downloadingW = (int) (barWidth * downloadingRatio);

            Gui.drawRect(barX - 1, barInnerY - 1, barX + barWidth + 1, barInnerY + barHeight + 1, 0xFF333333);
            Gui.drawRect(barX, barInnerY, barX + barWidth, barInnerY + barHeight, 0xFF1A1A1A);
            if (downloadedW > 0)
                Gui.drawRect(barX, barInnerY, barX + downloadedW, barInnerY + barHeight, 0xFF226622);
            if (downloadingW > 0)
                Gui.drawRect(barX + downloadedW, barInnerY, barX + downloadedW + downloadingW, barInnerY + barHeight, 0xFF885500);

            drawCenteredString(fontRendererObj, "telecharge : " + FileIntegrity.getDownloadedFileCount() + "/" + total + "  (" + FileIntegrity.getCurrentDownloads().size() + " en cours)", centerX, this.height - 12, 0x444444);
        }

        fontRendererObj.drawString("FSPatches v1.0.0", 8, this.height - 8, 0x333333);
        String mcVer = "Minecraft 1.7.10";
        fontRendererObj.drawString(mcVer, this.width - fontRendererObj.getStringWidth(mcVer) - 8, this.height - 8, 0x333333);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawDownloadList() {
        List<FileIntegrity.DownloadProgress> downloads = FileIntegrity.getCurrentDownloads();
        int y = 10;
        int max = 12;
        int count = 0;
        for (FileIntegrity.DownloadProgress progress : downloads) {
            if (count >= max) break;
            String text = progress.getFileName() + " - " + String.format("%.2f", progress.getPercentage()) + "%";
            this.fontRendererObj.drawStringWithShadow(text, 10, y, 0x555555);
            y += 10;
            count++;
        }
    }

    @Override
    public void onGuiClosed() {
        if (this.currentMusic != null)
            Minecraft.getMinecraft().getSoundHandler().stopSound(this.currentMusic);
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {}
}