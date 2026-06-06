package dev.anarchy.fspatches.patches.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.net.Proxy;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    // the easiest patch ever, just to remove the need to add the option in the launcher~!
    @Inject(method = "<init>", at = @At("RETURN"))
    public void addStencilBuffer(Session p_i1103_1_, int p_i1103_2_, int p_i1103_3_, boolean p_i1103_4_, boolean p_i1103_5_, File p_i1103_6_, File p_i1103_7_, File p_i1103_8_, Proxy p_i1103_9_, String p_i1103_10_, Multimap p_i1103_11_, String p_i1103_12_, CallbackInfo ci)
    {
        System.setProperty("forge.forceDisplayStencil", "true");
    }
}
