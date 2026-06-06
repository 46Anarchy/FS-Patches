package dev.anarchy.fspatches.content.blocks.luckyblocks;

import dev.anarchy.fspatches.content.luckyblocks.AbstractCustomLuckyBlock;
import dev.anarchy.fspatches.registering.annotation.RegisterBlock;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;

@RequiresMod("palamod")
@RegisterBlock("admin_abuse_lucky_block")
public class AdminAbuseLuckyBlock extends AbstractCustomLuckyBlock {
    public AdminAbuseLuckyBlock() {
        super("admin_abuse_lucky_block");
    }
}
