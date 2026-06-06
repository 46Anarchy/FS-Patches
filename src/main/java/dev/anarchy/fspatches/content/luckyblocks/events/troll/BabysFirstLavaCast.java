package dev.anarchy.fspatches.content.luckyblocks.events.troll;

import dev.anarchy.fspatches.content.blocks.luckyblocks.TrollLuckyBlock;
import dev.anarchy.fspatches.content.luckyblocks.EventRegister;
import dev.anarchy.fspatches.content.luckyblocks.LuckyUtils;
import fr.paladium.palaforgeutils.lib.inventory.InventoryUtils;
import fr.paladium.palaforgeutils.lib.scheduler.FMLServerScheduler;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

@EventRegister({TrollLuckyBlock.class})
public class BabysFirstLavaCast extends ALuckyEvent {

    @Override
    public void perform(EntityPlayerMP player, int x, int y, int z) {
        FMLServerScheduler.getInstance().add(() -> InventoryUtils.giveOrDropitems(player, new ItemStack(Items.water_bucket)));
        FMLServerScheduler.getInstance().add(() -> InventoryUtils.giveOrDropitems(player, new ItemStack(Items.lava_bucket)));
        FMLServerScheduler.getInstance().add(() -> InventoryUtils.giveOrDropitems(player, new ItemStack(Blocks.cobblestone, 64)));

        LuckyUtils.sendMessageTo(player, "Va faire un lavacast au spawn !");
    }

    @Override
    public String getName() {
        return "Baby's First LavaCast !";
    }
}
