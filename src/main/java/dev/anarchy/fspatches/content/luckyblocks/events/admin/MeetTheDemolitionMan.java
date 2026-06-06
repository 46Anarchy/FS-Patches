package dev.anarchy.fspatches.content.luckyblocks.events.admin;

import dev.anarchy.fspatches.content.blocks.luckyblocks.AdminAbuseLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import dev.anarchy.fspatches.content.luckyblocks.LuckyUtils;
import fr.paladium.palamod.api.ItemsRegister;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import fr.paladium.palamod.modules.luckyblock.utils.PlayerUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

// fuck it I just want to give bombs to everyone
@EventRegister({AdminAbuseLuckyBlock.class})
public class MeetTheDemolitionMan extends ALuckyEvent {

    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        PlayerUtils.dropItemStack(player.worldObj, x, y, z, new ItemStack(ItemsRegister.DYNAMITE_ENDIUM, 64).setStackDisplayName("Demoman's Old Reliable"));

        LuckyUtils.sendMessageTo(player, "Attention aux distances de securite");
    }

    @Override
    public String getName() {
        return "IF I WERE A BAD DEMOMAN...";
    }
}
