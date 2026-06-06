package dev.anarchy.fspatches.patches.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

@UtilityClass
public class ItemUtils {

    public static ItemStack addLore(ItemStack stack, String text) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagList lore;
        if (tag == null)
            tag = new NBTTagCompound();

        if (!tag.hasKey("display"))
            tag.setTag("display", new NBTTagCompound());

        if (tag.getCompoundTag("display").hasKey("Lore"))
            lore = tag.getCompoundTag("display").getTagList("Lore", 8);
        else
            lore = new NBTTagList();

        lore.appendTag(new NBTTagString(text));
        tag.getCompoundTag("display").setTag("Lore", lore);
        stack.setTagCompound(tag);
        return stack;
    }

    public static ItemStack addEnchantment(ItemStack stack, Enchantment enchant, int lvl)
    {
        if (!stack.hasTagCompound())
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        if (!stack.getTagCompound().hasKey("ench", 9))
        {
            stack.getTagCompound().setTag("ench", new NBTTagList());
        }

        NBTTagList nbttaglist = stack.stackTagCompound.getTagList("ench", 10);
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setShort("id", (short)enchant.effectId);
        nbttagcompound.setInteger("lvl", lvl);
        nbttaglist.appendTag(nbttagcompound);
        return stack;
    }

}
