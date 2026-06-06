package dev.anarchy.fspatches.content.luckyblocks.events.admin;

import dev.anarchy.fspatches.content.blocks.luckyblocks.AdminAbuseLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import dev.anarchy.fspatches.patches.utils.ItemUtils;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import fr.paladium.palamod.modules.luckyblock.utils.PlayerUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomItems;

@EventRegister({AdminAbuseLuckyBlock.class})
public class TheMalorian3516 extends ALuckyEvent {
    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        ItemStack gun = new ItemStack(CustomItems.gunIron, 1).setStackDisplayName("Malorian Arms 3516");

        ItemUtils.addLore(gun, "Has a mean streak. Like it's first owner.");
        ItemUtils.addLore(gun, "A Quick Melee Attack ignites the enemy and deals massive damage,");
        ItemUtils.addLore(gun, "but consumes all remaining bullets currently loaded in the gun.");

        gun.addEnchantment(Enchantment.fireAspect, 5);
        PlayerUtils.dropItemStack(player.worldObj, x, y, z, gun);
    }

    @Override
    public String getName() {
        return "The Johnny Silverhand";
    }
}
