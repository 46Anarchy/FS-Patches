package dev.anarchy.fspatches.content.items.stones;

import dev.anarchy.fspatches.content.items.AbstractCustomLegendaryStone;
import dev.anarchy.fspatches.patches.utils.BukkitUtils;
import dev.anarchy.fspatches.registering.annotation.RegisterItem;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;
import fr.paladium.palamod.modules.luckyblock.utils.EventUtils;
import glm.vec._3.d.Vec3d;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.Random;

@RequiresMod("palamod")
@RegisterItem("legendary_stone_spawn_stone")
public class SpawnLegendaryStone extends AbstractCustomLegendaryStone {
    public SpawnLegendaryStone() {
        super("SPAWN", Effect.TELEPORTATION);
    }

    @Override
    protected long getCooldown() {
        return 1000*60*10; // 10 minutes
    }

    @Override
    protected ItemStack onLGPop(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
        Random r = world.rand;

        double posX = r.nextInt(1000) - 500;
        double posZ = r.nextInt(1000) - 500;
        double posY = getIntTopBlock(world, (int)posX, (int)posZ);

        try {
            for(int tryCount = 0; !EventUtils.canInteract(entityPlayer, (int)posX, (int)posY, (int)posZ); ++tryCount) {
                if (tryCount >= 10) {
                    entityPlayer.addChatComponentMessage(new ChatComponentText("§8[§6Paladium§8] §7Une chose étrange est arrivée, la téléportation n'a pas fonctionné !"));
                }

                posX = r.nextInt(500) + -250;
                posZ = r.nextInt(500) + -250;
                posY = getIntTopBlock(world, (int)posX, (int)posZ);
            }
        } catch (Exception ignored) {}

        BukkitUtils.teleportPlayer(entityPlayer, new Vec3d(posX, posY, posZ));

        entityPlayer.addChatComponentMessage(new ChatComponentText("[FSPATCHES] : LG de Spawn !"));
        return itemStack;
    }
}
