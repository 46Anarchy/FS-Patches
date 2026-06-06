package dev.anarchy.fspatches.content.luckyblocks;

import fr.paladium.palamod.modules.luckyblock.utils.ALuckyEvent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;


public class CustomEntityLuckyBlock extends TileEntity {

    @Getter
    private boolean opened;

    @Getter
    private String ownerName;
    private String tileName;

    @Getter
    @Setter()
    private ALuckyEvent eventCL;
    private String event = null;

    public CustomEntityLuckyBlock(Class<? extends AbstractCustomLuckyBlock> aClass) {
        this.tileName = aClass.getSimpleName() + "_tile";
        this.ownerName = aClass.getSimpleName();
    }

    @Override
    public void readFromNBT(NBTTagCompound tags) {
        if (tags.hasKey("opened")) {
            opened = tags.getBoolean("opened");
        }
        if (tags.hasKey("ownerName")) {
            ownerName = tags.getString("ownerName");
            tileName = ownerName + "_tile";
        }
        if (tags.hasKey("event")) {
            event = tags.getString("event");
            eventCL = EventRegistry.getEventByClass(event);
            if (eventCL == null)
                throw new RuntimeException("what the fuck, eventCL is null !");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tags) {
        tags.setBoolean("opened", opened);
        tags.setString("ownerName", ownerName);
        if (event != null)
            tags.setString("event", eventCL.getClass().getName());
    }
}
