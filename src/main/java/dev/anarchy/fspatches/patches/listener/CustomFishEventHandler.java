package dev.anarchy.fspatches.patches.listener;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fr.paladium.palajobs.api.event.OnPlayerFish;
import fr.paladium.palajobs.core.entity.EntityCustomFishHook;
import fr.paladium.palajobs.core.jobs.requirement.FishingRequirement;
import fr.paladium.palajobs.core.network.data.JobsPlayer;
import fr.paladium.palajobs.core.pojo.objectives.types.FishingObjective;
import fr.paladium.palajobs.core.quest.types.FishingQuest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class CustomFishEventHandler {
    public CustomFishEventHandler() {
    }

    @SubscribeEvent
    public void onUseFishingRod(PlayerInteractEvent e) {
        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            EntityPlayer player = e.entityPlayer;
            World world = player.worldObj;
            if (!world.isRemote) {
                ItemStack item = player.inventory.getCurrentItem();
                if (item != null && item.getItem() instanceof ItemFishingRod && (e.face != 0 || e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {

                    EntityFishHook fishEntity = player.fishEntity;
                    boolean isFishing = fishEntity != null;
                    if (!isFishing) {
                        world.playSoundAtEntity(player, "random.bow", 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
                        if (!world.isRemote) {
                            world.spawnEntityInWorld(new EntityCustomFishHook(world, player));
                        }
                    } else {
                        fishEntity.setDead();
                    }
                    player.swingItem();

                    e.setResult(Event.Result.DENY);
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerFish(OnPlayerFish event) {
        EntityPlayer player = event.player;
        ItemStack reward = event.reward;
        if (!player.worldObj.isRemote) {
            FishingObjective.performCheck(player, reward);
            FishingQuest.performCheck(player, reward);
            JobsPlayer.get(player).getRequirements(FishingRequirement.class).forEach((optional) -> optional.ifPresent((requirement) -> requirement.perform(player, null)));
            fr.paladium.palapass.common.quest.misc.FishingQuest.trigger(player, reward, reward.stackSize);
        }
    }
}
