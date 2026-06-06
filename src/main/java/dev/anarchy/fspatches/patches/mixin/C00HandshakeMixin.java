package dev.anarchy.fspatches.patches.mixin;

import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.handshake.client.C00Handshake;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(C00Handshake.class)
public class C00HandshakeMixin {
    @Shadow
    private int field_149600_a;

    @Inject(method = "<init>(ILjava/lang/String;ILnet/minecraft/network/EnumConnectionState;)V", at = @At("RETURN"))
    public void onInitPatch(int p_i45266_1_, String p_i45266_2_, int p_i45266_3_, EnumConnectionState p_i45266_4_, CallbackInfo ci) {
        this.field_149600_a = 5;
    }
}
