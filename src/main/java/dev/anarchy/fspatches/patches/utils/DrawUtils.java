package dev.anarchy.fspatches.patches.utils;

import fr.paladium.paladiumui.kit.font.PaladiumFont;
import fr.paladium.zephyrui.lib.color.Color;
import fr.paladium.zephyrui.lib.font.dto.text.TextInfo;
import fr.paladium.zephyrui.lib.utils.align.Align;

public class DrawUtils {
    public static void drawDiscord() {
        fr.paladium.zephyrui.lib.draw.DrawUtils.TEXT.drawText(960, 910, "https://discord.46anarchy.fr", TextInfo.create(PaladiumFont.MONTSERRAT_MEDIUM, 26.0F, Color.WHITE).letterSpacing(4.0f), Align.CENTER, Align.CENTER);
    }
}
