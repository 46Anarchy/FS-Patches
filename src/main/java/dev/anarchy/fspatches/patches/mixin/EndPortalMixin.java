package dev.anarchy.fspatches.patches.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "fr.paladium.palamod.modules.end.server.event.ServerEventHandler", remap = false)
public abstract class EndPortalMixin {

    @Inject(
            method = "onPortalEnter(Lfr/paladium/palamod/events/EntityPortalEnterEvent;)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void cancelPortalEnter(CallbackInfo ci) {
        ci.cancel();
    }
}
