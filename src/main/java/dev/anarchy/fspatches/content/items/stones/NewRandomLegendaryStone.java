package dev.anarchy.fspatches.content.items.stones;

import dev.anarchy.fspatches.content.items.AbstractCustomLegendaryStone;
import dev.anarchy.fspatches.registering.AnnotationScanner;
import dev.anarchy.fspatches.registering.annotation.RegisterItem;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;
import fr.paladium.palaforgeutils.lib.inventory.InventoryUtils;
import fr.paladium.palaforgeutils.lib.scheduler.FMLServerScheduler;
import fr.paladium.palamod.api.ItemsRegister;
import fr.paladium.palamod.modules.paladium.common.items.LegendaryStone;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// replace the palamod Random legendary stone because it does not give
// our custom legendary stones :(
@RequiresMod("palamod")
@RegisterItem("legendary_stone_new_random")
public class NewRandomLegendaryStone extends AbstractCustomLegendaryStone {
    public NewRandomLegendaryStone() {
        super("RANDOM");
    }

    @Override
    protected long getCooldown() {
        return 0;
    }

    @Override
    protected ItemStack onLGPop(ItemStack itemStack, World world, EntityPlayer entityPlayer) {

        List<Item> stoneList = new ArrayList<>(Arrays.asList(
                ItemsRegister.LEGENDARYSTONE_TELEPORTATION,
                ItemsRegister.LEGENDARYSTONE_INVISIBILITY,
                ItemsRegister.LEGENDARYSTONE_FORTUNE,
                ItemsRegister.LEGENDARYSTONE_POWER,
                ItemsRegister.LEGENDARYSTONE_JOBS,
                ItemsRegister.LEGENDARYSTONE_CHAOS,
                ItemsRegister.LEGENDARYSTONE_TELEPORTATION
        ));

        if (AnnotationScanner.isItemAvailable("legendary_stone_spawn_stone"))
            stoneList.add(AnnotationScanner.getItem("legendary_stone_spawn_stone"));
        if (AnnotationScanner.isItemAvailable("legendary_stone_kamikaze_stone"))
            stoneList.add(AnnotationScanner.getItem("legendary_stone_kamikaze_stone"));

        Item[] stones = stoneList.toArray(new Item[0]);
        int effectId = (int)(Math.random() * (stones.length - 1));

        ItemStack itemGive = new ItemStack(stones[effectId]);

        --itemStack.stackSize;
        FMLServerScheduler.getInstance().add(() -> InventoryUtils.removeItems(entityPlayer, itemStack, 1));
        FMLServerScheduler.getInstance().add(() -> InventoryUtils.giveOrDropitems(entityPlayer, itemGive));

        String name = itemGive.getItem() instanceof AbstractCustomLegendaryStone ?
                ((AbstractCustomLegendaryStone)itemGive.getItem()).getName() : ((LegendaryStone)itemGive.getItem()).getEffect().getDisplayName();

        entityPlayer.addChatComponentMessage(new ChatComponentText("§8[§6Paladium§8] §7Tu as reçu un super item, une LegendaryStone de " + name + ", mais à quoi cela sert-il ?"));
        return itemStack;
    }
}
