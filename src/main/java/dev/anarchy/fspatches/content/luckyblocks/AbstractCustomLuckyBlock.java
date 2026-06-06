package dev.anarchy.fspatches.content.luckyblocks;

import dev.anarchy.fspatches.registering.AnnotationScanner;
import fr.paladium.palamod.modules.luckyblock.PLuckyBlock;
import fr.paladium.palamod.modules.luckyblock.tileentity.TileEntityLuckyBlock;
import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import fr.paladium.palamod.modules.luckyblock.utils.LuckyEvents;
import fr.paladium.palamod.modules.luckyblock.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.*;

public abstract class AbstractCustomLuckyBlock extends Block {

    protected final String name;

    private Set<ALuckyEvent> events = new HashSet<>();

    public AbstractCustomLuckyBlock(String name) {
        super(Material.iron);
        this.setBlockName(name);
        this.setBlockTextureName("fspatches:" + name);
        this.setHardness(1.0f);
        this.setResistance(1000.0f);
        this.setCreativeTab(PLuckyBlock.TAB);
        this.name = name;

        events.addAll(EventRegistry.getEventsForType(this.getClass()));

        // there is a high probability that this is the worst idea I ever got
        // - Morgane
        AnnotationScanner.tryRegisterTileEntity(CustomEntityLuckyBlock.class, this.name + "_tile");
    }

    public List<ALuckyEvent> getEvents() {
        events.addAll(EventRegistry.getEventsForType(this.getClass()));
        return new ArrayList<>(events);
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new CustomEntityLuckyBlock(this.getClass());
    }

    // ok what the fuck is this shit doing ?
    // it seems to set tile entity data based on itemstack data (what the fuck ?!)
    // is this tied to some code to have a specific event when /give'd to someone ?
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
        /* TODO REMOVE THIS, THIS IS UPSTREAM CODE */
        TileEntityLuckyBlock te = (TileEntityLuckyBlock)world.getTileEntity(x, y, z);
        if (te != null) {
            // ok so this write data into the tile entity data...
            // EXCEPT IT DOES NOT HAVE ANY ?!
            // fuck this, cant wait to be home and fuck off
            te.func_145841_b(new NBTTagCompound());
            if (stack.hasTagCompound()) {
                if (stack.getTagCompound().hasKey("event")) {
                    te.setEvent(LuckyEvents.values()[stack.getTagCompound().getInteger("event")]);
                }
                // WHAT THE FUCK IS A VERSION TAG EVEN DOING HERE
                // I DID NOT SEE THIS GETTING USED A SINGLE FUCKING TIME
                if (stack.getTagCompound().hasKey("version")) {
                    te.setVersion(stack.getTagCompound().getInteger("version"));
                }
            }

        }
    }

    // if I understood correctly, does not drop the item if currently playing an event,
    // otherwise drop the block... I think ?
    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        if (!world.isRemote) {
            CustomEntityLuckyBlock te = (CustomEntityLuckyBlock) world.getTileEntity(x, y, z);
            if (te != null) {
                if (te.getEventCL() != null) {
                    te.getEventCL().perform((EntityPlayerMP) player, x, y, z);
                } else {
                    PlayerUtils.dropItemStack(world, x, y, z, new ItemStack(this));
                }
            }
        }
    }

    // do all the shit we need it to
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            if (getEvents().isEmpty()) {
                LuckyUtils.sendMessageTo((EntityPlayerMP) player, "Some-fucking-how the event list is empty.");
                return true;
            }
            Random rand = new Random();
            int event = rand.nextInt(getEvents().size());

            getEvents().get(event).perform((EntityPlayerMP) player, x, y, z);

            world.setBlockToAir(x, y, z);
        }
        return true;
    }
}
