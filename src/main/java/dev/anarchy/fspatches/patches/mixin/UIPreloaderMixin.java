package dev.anarchy.fspatches.patches.mixin;

import fr.paladium.palamod.modules.paladium.client.gui.palamenu.preloader.UIPreLoader;
import fr.paladium.zephyrui.lib.draw.DrawUtils;
import fr.paladium.zephyrui.lib.draw.resource.DrawResource;
import fr.paladium.zephyrui.lib.resource.Resource;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(UIPreLoader.class)
public class UIPreloaderMixin {

    @Shadow
    public static Resource BACKGROUND_TEXTURE;

    @Shadow
    public static Resource LOGO_TEXTURE;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void onInitPatch(CallbackInfo ci) {
        BACKGROUND_TEXTURE = Resource.of(new ResourceLocation("fspatches", "textures/background/play.png"));
        LOGO_TEXTURE = Resource.of(new ResourceLocation("fspatches", "textures/logo.png"));
//        UIMainMenu.loaded = true;
//        Minecraft.getMinecraft().displayGuiScreen(new UIMainMenu());
    }

    @Inject(method = "init", at = @At("TAIL"), remap = false)
    public void onInitPatchGuiScreen(CallbackInfo ci) {
        onInitPatch(ci);
    }

    @Inject(method = "checkStatus", at = @At("HEAD"), remap = false, cancellable = true)
    public void onCheckStatus(CallbackInfoReturnable<CompletableFuture<Boolean>> cir) {
        cir.setReturnValue(CompletableFuture.completedFuture(true));
    }

    @Redirect(
            method = "postDraw",
            at = @At(
                    value = "INVOKE",
                    target = "Lfr/paladium/zephyrui/lib/draw/resource/DrawResource;drawImage(DDDDLfr/paladium/zephyrui/lib/resource/Resource;)V",
                    ordinal = 0
            ),
            remap = false
    )
    private void resizeLogo(DrawResource instance, double x, double y, double width, double height, Resource resource) {
        DrawUtils.RESOURCE.drawImage(640.0, 180.0, 640.0, 640.0, resource);
    }
}
