package dev.anarchy.fspatches.content.command;

import fr.paladium.palaforgeutils.lib.command.SenderType;
import fr.paladium.palaforgeutils.lib.command.annotated.annotation.Command;
import fr.paladium.palaforgeutils.lib.command.annotated.annotation.SubCommand;
import fr.paladium.palaforgeutils.lib.command.annotated.context.CommandContext;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;

@Command(
        command = {"discord"},
        description = "Commande pour afficher le lien du discord",
        permission = "46.command.discord"
)
public class DiscordCommand {
    public DiscordCommand() {}

    @SubCommand(
            command = "discord",
            description = "Recuperer le lien du discord",
            permission = "46.command.discord",
            sender = {SenderType.PLAYER}
    )
    public void open(CommandContext context) {
        ChatComponentText message = new ChatComponentText("§8[§646Anarchy§8] §7Rejoignez notre discord en cliquant ici !");
        message.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.46anarchy.fr"));
        context.getPlayer().addChatMessage(message);
    }
}
