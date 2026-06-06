package dev.anarchy.fspatches.content.items;

import fr.paladium.palaforgeutils.lib.task.DurationConverter;
import fr.paladium.palamod.PalaMod;
import fr.paladium.palamod.common.Registry;
import fr.paladium.palamod.modules.achievements.types.UseItemAchievement;
import fr.paladium.palamod.modules.paladium.common.eep.LegendaryStoneEEP;
import fr.paladium.palamod.modules.paladium.common.items.LegendaryStone;
import fr.paladium.palamod.modules.paladium.network.PacketLegendaryEffect;
import fr.paladium.pet.common.network.data.PetPlayer;
import fr.paladium.pet.server.skill.handler.PassiveResponse;
import fr.paladium.pet.server.skill.handler.PassiveSkillEnum;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.concurrent.TimeUnit;

public abstract class AbstractCustomLegendaryStone extends LegendaryStone {

    @Getter
    private String name;

    public AbstractCustomLegendaryStone(String name) {
        super(Effect.RANDOM);
        this.name = name;
        this.setUnlocalizedName("stone_" + name);
        this.setCreativeTab(Registry.PALADIUM_TAB);
    }

    public AbstractCustomLegendaryStone(String name, Effect effect) {
        super(effect);
        this.name = name;
        this.setUnlocalizedName("stone_" + name);
        this.setCreativeTab(Registry.PALADIUM_TAB);
    }

    private long applyPetSkill(EntityPlayer player, long cooldownTime) {
        PetPlayer pet = PetPlayer.get(player);
        PassiveResponse response = PassiveSkillEnum.LEGENDARY_EXPERT.getResponse(pet);
        double value = response.getPersonalValue(pet);
        if (!response.has(value)) {
            return cooldownTime;
        } else {
            long minutes = (long)((double)60.0F * value);
            return minutes <= 0L ? cooldownTime : cooldownTime - TimeUnit.MINUTES.toMillis(minutes);
        }
    }

    private long accessPetSkill(EntityPlayer player, long time) {
        return applyPetSkill(player, time);
    }

    private long accessGetTime(EntityPlayer player) {
        String name = "legendary_" + getName();
        return player.getEntityData().hasKey(name) ? player.getEntityData().getLong(name) : 0L;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        if (!world.isRemote) {
            long itemCooldown = getCooldown();
            String lastUseKey = "LAST_USE";

            long playerCooldown = accessPetSkill(entityPlayer, 3600000L);

            itemCooldown = this.accessPetSkill(entityPlayer, itemCooldown);

            long playerTime = this.accessGetTime(entityPlayer);
            long itemTime = itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey(lastUseKey) ? itemStack.getTagCompound().getLong(lastUseKey) : 0L;

            if (playerTime + playerCooldown > System.currentTimeMillis()) {
                entityPlayer.addChatComponentMessage(new ChatComponentText("§8[§6Paladium§8] §7Tu dois encore attendre " + DurationConverter.fromMillisToString(playerTime + playerCooldown - System.currentTimeMillis()) + " avant de pouvoir l'utiliser ce type de legendary stone."));
                return itemStack;
            } else if (itemTime + itemCooldown > System.currentTimeMillis()) {
                entityPlayer.addChatComponentMessage(new ChatComponentText("§8[§6Paladium§8] §7Tu dois encore attendre " + DurationConverter.fromMillisToString(itemTime + itemCooldown - System.currentTimeMillis()) + " avant de pouvoir utiliser cette legendary stone."));
                return itemStack;
            } else {
                UseItemAchievement.performCheck(entityPlayer, itemStack, itemStack.getDisplayName());
                LegendaryStoneEEP eep = LegendaryStoneEEP.get(entityPlayer);
                if (eep != null) {
                    eep.increaseUse(this.getEffect().name());
                }

                //the custom legendary stone instead of one fucking big switch statement
                itemStack = onLGPop(itemStack, world, entityPlayer);

                // used exclusively for the new random stone so that it create proper particles for the stone it gives
                Effect effect = itemStack.getItem() instanceof LegendaryStone ? ((LegendaryStone)itemStack.getItem()).getEffect() : getEffect();
                PalaMod.getNetwork().sendTo(new PacketLegendaryEffect(effect.getRed(), effect.getGreen(), effect.getBlue(), effect), (EntityPlayerMP) entityPlayer);

                // are we sure the paladium way is efficient ?
                // or is it a decompilation artifact ?
                NBTTagCompound nbtTagCompound = itemStack.getTagCompound();
                nbtTagCompound.removeTag(lastUseKey);
                nbtTagCompound.setLong(lastUseKey, System.currentTimeMillis());
                itemStack.setTagCompound(nbtTagCompound);
            }
        }
        return itemStack;
    }

    protected abstract long getCooldown();

    protected abstract ItemStack onLGPop(ItemStack itemStack, World world, EntityPlayer entityPlayer);
}
