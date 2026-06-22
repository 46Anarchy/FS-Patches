package dev.anarchy.fspatches.content.misc;

import fr.paladium.factions.server.permissions.IPermissionProvider;
import fr.paladium.permissionbridge.common.data.PermissibleEntity;
import fr.paladium.permissionbridge.common.manager.PermissionManager;

import java.util.UUID;

public class FsPermissionProvider implements IPermissionProvider {
    @Override
    public String getPrimaryGroupName(UUID uuid) {
        return "default";
    }

    @Override
    public String getPrimaryGroupPrefix(UUID uuid) {
        return PermissionManager.inst().getPermission(PermissibleEntity.from(uuid), "fspatches.group.prefix.", String.class).orElse("§7Joueur");
    }

    @Override
    public String getPrimaryGroupSuffix(UUID uuid) {
        return "";
    }
}
