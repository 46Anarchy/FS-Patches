package dev.anarchy.fspatches.patches.mixin;

import fr.paladium.palamod.util.MixinBuilderHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MixinBuilderHelper.class, remap = false)
public class BuilderHelperPatchMixin {

    @Shadow
    private static int minX;

    @Shadow
    private static int maxX;

    @Shadow
    private static int minZ;

    @Shadow
    private static int maxZ;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void reduceArea(CallbackInfo ci)
    {
        minX = 0;
        maxX = 0;
        minZ = 0;
        maxZ = 0;
    }
}
