package dev.anarchy.fspatches.patches.mixin;

import fr.paladium.palamod.modules.paladium.client.gui.palamenu.mainmenu.utils.connector.ServerSession;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ServerSession.class)
public class ServerSessionMixin {
    @Mutable
    @Shadow
    @Final
    private int port;

    @Mutable
    @Shadow
    @Final
    private String host;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    public void onInitPatch(String host, int port, Consumer callback, CallbackInfo ci) {
        this.port = 25566;
        if (!host.equalsIgnoreCase("localhost") && !host.equalsIgnoreCase("127.0.0.1"))
            this.host = "mc.46anarchy.fr";
    }
}

