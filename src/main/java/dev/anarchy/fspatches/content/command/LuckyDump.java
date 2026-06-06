package dev.anarchy.fspatches.content.command;

import fr.paladium.palaforgeutils.lib.command.SenderType;
import fr.paladium.palaforgeutils.lib.command.annotated.annotation.Command;
import fr.paladium.palaforgeutils.lib.command.annotated.annotation.SubCommand;
import fr.paladium.palaforgeutils.lib.command.annotated.context.CommandContext;
import fr.paladium.palamod.modules.luckyblock.tileentity.TileEntityLuckyBlock;
import net.minecraft.tileentity.TileEntity;


@Command(command = {"luckydump"}, description = "dump data about a lucky block", permission = "46.command.luckydump")
public class LuckyDump {
    @SubCommand(command = "luckydump <x> <y> <z>",
            description = "dump data about a lucky block",
            permission = "46.command.luckydump",
            sender = {SenderType.PLAYER})
    public void dump(CommandContext ctx, int x, int y, int z) {
        if (ctx.getPlayer() == null) {
            ctx.error("Cannot execute this command in the console !");
            return;
        }

        if (ctx.getArgs().length < 3) {
            ctx.error("Invalid argument ! Usage : /luckydump <x> <y> <z>");
            return;
        }

        TileEntity tile = ctx.getPlayer().worldObj.getTileEntity(x, y, z);

        if (!(tile instanceof TileEntityLuckyBlock)) {
            ctx.error("Tile is either null or not a luckyblock !");
            return ;
        }

        TileEntityLuckyBlock luckyBlock = (TileEntityLuckyBlock) tile;

        ctx.send("type: " + luckyBlock.getType().getText());
        ctx.send("opened: " + luckyBlock.isOpen());
        ctx.send("version: " + luckyBlock.getVersion());
        ctx.send("event class: " + luckyBlock.getEvent() == null ? "NONE" : (luckyBlock.getEvent().name() + "@" + luckyBlock.getEvent().getEvent().getClass().getName()));
    }
}
