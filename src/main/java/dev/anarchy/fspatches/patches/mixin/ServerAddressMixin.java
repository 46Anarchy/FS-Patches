package dev.anarchy.fspatches.patches.mixin;

import net.minecraft.client.multiplayer.ServerAddress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerAddress.class)
abstract class ServerAddressMixin {

    @Shadow
    @Final
    private String ipAddress;

    @Inject(method = "getIP", at = @At("HEAD"), cancellable = true)
    private void getIPPatch(CallbackInfoReturnable<String> cir) {
        if (ipAddress.equalsIgnoreCase("localhost") && !ipAddress.equalsIgnoreCase("127.0.0.1"))
            cir.setReturnValue("mc.46anarchy.fr");
    }

    @Inject(method = "getPort", at = @At("HEAD"), cancellable = true)
    private void getPortPatch(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(25566);
    }

}
