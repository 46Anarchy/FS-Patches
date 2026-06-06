package dev.anarchy.fspatches.content.luckyblocks.events.admin;

import dev.anarchy.fspatches.content.blocks.luckyblocks.AdminAbuseLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import dev.anarchy.fspatches.content.luckyblocks.LuckyUtils;
import fr.paladium.palamod.api.ItemsRegister;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import fr.paladium.palamod.modules.luckyblock.utils.PlayerUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

@EventRegister({AdminAbuseLuckyBlock.class})
public class GoCommitACrime extends ALuckyEvent
{
    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        ItemStack item = new ItemStack(ItemsRegister.BUTCHER_KNIFE);

        NBTTagCompound compound = new NBTTagCompound();
        NBTTagCompound display = new NBTTagCompound();
        NBTTagList lore = new NBTTagList();

        lore.appendTag(new NBTTagString(" They say my hunger's a problem"));
        lore.appendTag(new NBTTagString(" They tell me to curb my appetite"));

        display.setTag("Lore", lore);
        compound.setTag("display", display);
        item.setTagCompound(compound);

        item.setStackDisplayName("Butcher's Vanity <3");
        item.addEnchantment(Enchantment.sharpness, 10);
        item.addEnchantment(Enchantment.unbreaking, 10);

        PlayerUtils.dropItemStack(player.worldObj, x, y, z, item);
        LuckyUtils.sendMessageTo(player, "La boucherie est ouverte ;)");
    }

    @Override
    public String getName() {
        return "Go commit a crime";
    }
}
