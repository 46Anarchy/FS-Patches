package dev.anarchy.fspatches.content.blocks.luckyblocks;

import dev.anarchy.fspatches.content.luckyblocks.AbstractCustomLuckyBlock;
import dev.anarchy.fspatches.registering.annotation.RegisterBlock;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;

@RequiresMod("palamod")
@RegisterBlock("troll_lucky_block")
public class TrollLuckyBlock extends AbstractCustomLuckyBlock {
    public TrollLuckyBlock() {
        super("troll_lucky_block");
    }
}
