package dev.anarchy.fspatches.content.luckyblocks;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

public class LuckyUtils {
    public static void sendMessageTo(EntityPlayerMP player, String message) {
        player.addChatComponentMessage(new ChatComponentText("§8[§dFSLuckyBlock§8]§r " + message));
    }
}
