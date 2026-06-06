package dev.anarchy.fspatches.content.luckyblocks.events.admin;

import com.jaquadro.minecraft.storagedrawers.core.ModBlocks;
import dev.anarchy.fspatches.content.blocks.luckyblocks.AdminAbuseLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

@EventRegister({AdminAbuseLuckyBlock.class})
public class LogoInTheSky extends ALuckyEvent {
    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        ItemStack stack = new ItemStack(ModBlocks.fullDrawers1);
    }

    @Override
    public String getName() {
        return "Gros logo en obby";
    }
}
