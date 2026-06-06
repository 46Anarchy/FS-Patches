package dev.anarchy.fspatches.content.luckyblocks.events.troll;

import dev.anarchy.fspatches.content.luckyblocks.LuckyUtils;
import fr.paladium.palaforgeutils.lib.inventory.InventoryUtils;
import fr.paladium.palaforgeutils.lib.scheduler.FMLServerScheduler;
import fr.paladium.palamod.api.ItemsRegister;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

// this event should never work since it does not have the @EventRegister
// ... At least for now
public class Audiophile extends ALuckyEvent {

    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        Item[] items = {
                ItemsRegister.DISC_ANTI_FUZE,
                ItemsRegister.DISC_FUZEIII,
                ItemsRegister.DISC_CLASH_KUMIZ,
                ItemsRegister.DISC_MII,
                ItemsRegister.DISC_NATIONS_GLORY,
                ItemsRegister.DISC_HALLOWEEN,
                ItemsRegister.DISC_NOEL,
                ItemsRegister.DISC_PALADIUM_BEST_SOUND,
                ItemsRegister.DISC_ROULETTE_PALADIENNE
        };

        for (Item item : items) {
            FMLServerScheduler.getInstance().add(() -> InventoryUtils.giveOrDropitems(player, new ItemStack(item, 1)));
        }

        LuckyUtils.sendMessageTo(player, "J'espere que tu aimes la musique !");
    }

    @Override
    public String getName() {
        return "Audiophile";
    }
}
