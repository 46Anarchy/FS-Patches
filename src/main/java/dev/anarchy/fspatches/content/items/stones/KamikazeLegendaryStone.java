package dev.anarchy.fspatches.content.items.stones;

import dev.anarchy.fspatches.content.items.AbstractCustomLegendaryStone;
import dev.anarchy.fspatches.registering.annotation.RegisterItem;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;
import fr.paladium.palamod.modules.paladium.common.entities.projectiles.DynamiteEndiumEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

@RequiresMod("palamod")
@RegisterItem("legendary_stone_kamikaze_stone")
public class KamikazeLegendaryStone extends AbstractCustomLegendaryStone {
    public KamikazeLegendaryStone() {
        super("KAMIKAZE", Effect.CHAOS);
    }

    @Override
    protected long getCooldown() {
        return 1000*60*60; // one hour
    }

    @Override
    protected ItemStack onLGPop(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        for (int i = 0; i < 16; i++) {
            DynamiteEndiumEntity dyna = new DynamiteEndiumEntity(world, entityPlayer, 5, 0);
            world.spawnEntityInWorld(dyna);
        }

        entityPlayer.addChatComponentMessage(new ChatComponentText("[FSPATCHES] : LG de kamikaze !"));
        return itemStack;
    }
}
