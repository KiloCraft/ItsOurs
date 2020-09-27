package me.drex.itsours.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class PermissionHandler {

    private boolean present;

    public PermissionHandler() {
        this.present = checkPresent();
        if (this.present) {
            ItsOursMod.LOGGER.info("Using Luckperms as the Permission Manager");
        } else {
            ItsOursMod.LOGGER.info("Using Vanilla operator system as the Permission Manager");
        }
    }

    public boolean hasPermission(ServerCommandSource src, String permission, int opLevel) {
        LuckPerms luckPerms = LuckPermsProvider.get();

        try {
            ServerPlayerEntity player = src.getPlayer();
            User user = luckPerms.getUserManager().getUser(player.getUuid());

            if (user != null) {
                QueryOptions options = luckPerms.getContextManager().getQueryOptions(player);
                return user.getCachedData().getPermissionData(options).checkPermission(permission).asBoolean();
            }

        } catch (CommandSyntaxException ignored) {
        }

        return src.hasPermissionLevel(opLevel);
    }

        private boolean checkPresent() {
        try {
            LuckPermsProvider.get();
            return true;
        } catch (IllegalStateException ignored) {
        }
        return false;
    }

}
