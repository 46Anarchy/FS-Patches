package dev.anarchy.fspatches.content.blocks;

import dev.anarchy.fspatches.patches.utils.BukkitUtils;
import dev.anarchy.fspatches.registering.AnnotationScanner;
import dev.anarchy.fspatches.registering.FSRecipe;
import dev.anarchy.fspatches.registering.ICraftable;
import dev.anarchy.fspatches.registering.annotation.RegisterBlock;
import dev.anarchy.fspatches.registering.annotation.RequiresMod;
import fr.paladium.palaforgeutils.lib.inventory.InventoryUtils;
import fr.paladium.palaforgeutils.lib.scheduler.FMLServerScheduler;
import fr.paladium.palamod.api.ItemsRegister;
import glm.vec._3.d.Vec3d;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import org.bukkit.Bukkit;

@RequiresMod("palamod")
@RegisterBlock("stasis_chamber_block")
public class StasisChamberBlock extends Block implements ICraftable {

    private final IIcon[] textures = new IIcon[4];

    public StasisChamberBlock() {
        super(Material.iron);
        this.setBlockName("stasis_chamber_block");
        AnnotationScanner.tryRegisterTileEntity(StasisChamberTileEntity.class, "stasis_chamber_tile_entity");
    }

    @Override
    public boolean hasTileEntity() {
        return true;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new StasisChamberTileEntity();
    }

    @Override
    public void registerBlockIcons(IIconRegister register) {
        this.textures[0] = register.registerIcon("fspatches:stasis_chamber/stasis_chamber_side_off");
        this.textures[1] = register.registerIcon("fspatches:stasis_chamber/stasis_chamber_side_on");
        this.textures[2] = register.registerIcon("fspatches:stasis_chamber/stasis_chamber_top");
        this.textures[3] = register.registerIcon("fspatches:stasis_chamber/stasis_chamber_bottom");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 1)
            return this.textures[2];
        if (side == 0)
            return this.textures[3];
        return textures[meta];
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block side) {
        if (!world.isRemote) {
            if (world.isBlockIndirectlyGettingPowered(x, y, z)) {
                TileEntity tile = world.getTileEntity(x, y, z);

                if (tile instanceof StasisChamberTileEntity) {
                    StasisChamberTileEntity entity = (StasisChamberTileEntity) tile;
                    int meta = entity.getBlockMetadata();
                    if (BukkitUtils.isPlayerOnline(entity.username) && meta == 1) {
                        BukkitUtils.teleportPlayer(Bukkit.getPlayer(entity.username), new Vec3d(x + 0.5, y + 1, z + 0.5));
                    }
                    entity.username = "";
                    world.setBlockMetadataWithNotify(x, y, z, 0, 3);
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(x, y, z);

            if (tile instanceof StasisChamberTileEntity) {
                StasisChamberTileEntity entity = (StasisChamberTileEntity) tile;

                if (entity.username == null || entity.username.isEmpty()) {
                    if (player.getHeldItem() != null && player.getHeldItem().getItem().equals(Items.ender_pearl)) {
                        FMLServerScheduler.getInstance().add(() -> InventoryUtils.removeItems(player, player.getHeldItem(), 1));
                        entity.username = player.getDisplayName();
                        world.setBlockMetadataWithNotify(x, y, z, 1, 3);
                        player.addChatComponentMessage(new ChatComponentText("[FSPatches] Cette chambre est desormais la votre. Activez un signal de redstone dessus pour vous teleporter dessus."));
                    }
                    else
                        player.addChatComponentMessage(new ChatComponentText("[FSPatches] Faites click droit sur ce block avec une enderpeal."));
                }
                else
                    player.addChatComponentMessage(new ChatComponentText("[FSPatches] Cette chambre est utilisée par " + entity.username + "."));
            }

            return true;
        }
        return false;
    }

    @Override
    public FSRecipe getRecipe() {
        return new FSRecipe().setRecipe(new ItemStack[][]{
                {new ItemStack(ItemsRegister.PALADIUM_INGOT), new ItemStack(Items.redstone), new ItemStack(ItemsRegister.PALADIUM_INGOT)},
                {new ItemStack(Items.redstone),               new ItemStack(Items.ender_pearl), new ItemStack(Items.redstone)},
                {new ItemStack(ItemsRegister.PALADIUM_INGOT), new ItemStack(Items.redstone), new ItemStack(ItemsRegister.PALADIUM_INGOT)},
        });
    }

    public static class StasisChamberTileEntity extends TileEntity {

        public String username = "";

        @Override
        public void readFromNBT(NBTTagCompound p_145839_1_) {
            super.readFromNBT(p_145839_1_);
            this.username = p_145839_1_.getString("username");
        }

        @Override
        public void writeToNBT(NBTTagCompound p_145841_1_) {
            super.writeToNBT(p_145841_1_);
            p_145841_1_.setString("username", this.username);
        }
    }
}
