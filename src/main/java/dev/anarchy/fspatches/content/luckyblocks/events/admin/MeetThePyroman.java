package dev.anarchy.fspatches.content.luckyblocks.events.admin;

import dev.anarchy.fspatches.content.blocks.luckyblocks.AdminAbuseLuckyBlock;
import dev.anarchy.fspatches.content.blocks.luckyblocks.TrollLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import dev.anarchy.fspatches.content.luckyblocks.LuckyUtils;
import dev.anarchy.fspatches.patches.utils.ItemUtils;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import fr.paladium.palamod.modules.luckyblock.utils.PlayerUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

// test with event in several lbs
// and also test the ItemUtils#addLore
@EventRegister({AdminAbuseLuckyBlock.class, TrollLuckyBlock.class})
public class MeetThePyroman extends ALuckyEvent {
    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        ItemStack flint = new ItemStack(Items.flint_and_steel).setStackDisplayName("Dragon's Fury");

        flint.setRepairCost(Integer.MAX_VALUE);

        ItemUtils.addEnchantment(flint, Enchantment.unbreaking, 32767);
        flint.addEnchantment(Enchantment.fireAspect, 2);

        ItemUtils.addLore(flint, " Do you believe in magic in a young girl's heart ?");
        ItemUtils.addLore(flint, " How the music can free her, whenever it starts");
        ItemUtils.addLore(flint, " And it's magic, if the music is groovy");
        ItemUtils.addLore(flint, " It makes you feel happy like an old-time movie");

        PlayerUtils.dropItemStack(player.worldObj, x, y, z, flint);

        LuckyUtils.sendMessageTo(player, "https://www.youtube.com/watch?v=K_t6yq6D6xc");
    }

    @Override
    public String getName() {
        return "Some men just want to watch the world burn...";
    }
}
