package dev.anarchy.fspatches.patches.mixin;

import fr.paladium.palamod.PalaMod;
import fr.paladium.palamod.common.guihandler.PGuiRegistry;
import fr.paladium.palamod.modules.factions.PFactions;
import fr.paladium.palamod.modules.paladium.common.blocks.BlockAnvil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAnvil.class)
public class BlockAnvilMixin {
    @Inject(method = "func_149727_a", at = @At("HEAD"), cancellable = true, remap = false)
    private void AnvilNPEFix(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {

        if (PFactions.instance == null || PFactions.instance.getImpl() == null) {
            player.openGui(PalaMod.instance, PGuiRegistry.GUI_ANVIL, world, x, y, z);
            cir.setReturnValue(true);
        }
    }
}