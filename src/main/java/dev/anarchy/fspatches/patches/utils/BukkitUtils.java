package dev.anarchy.fspatches.patches.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import glm.vec._3.d.Vec3d;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.minecraft.entity.player.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@UtilityClass
public class BukkitUtils {

    @SideOnly(Side.SERVER)
    @SneakyThrows
    public static void teleportPlayer(EntityPlayer entity, Vec3d pos)
    {
        Player player = Bukkit.getPlayer(entity.getUniqueID());
        Location location = new Location(player.getWorld(), pos.x, pos.y, pos.z);
        player.teleport(location);
    }
}
