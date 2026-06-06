package dev.anarchy.fspatches.content.blocks;

import dev.anarchy.fspatches.registering.annotation.RegisterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

@RegisterBlock("test_block")
public class test extends Block {
    protected test() {
        super(Material.air);
    }
}
