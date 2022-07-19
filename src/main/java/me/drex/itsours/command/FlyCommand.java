package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.user.Settings;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class FlyCommand extends ToggleCommand {

    public static final FlyCommand INSTANCE = new FlyCommand();

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> ItsOurs.hasPermission(src, "fly")));
    }

    private FlyCommand() {
        super("fly", Settings.FLIGHT, "text.itsours.commands.fly");
    }

    @Override
    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();
        if (ClaimList.INSTANCE.getClaimAt(player).isPresent() && player.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            if (newValue) {
                player.getAbilities().allowFlying = true;
            }
            player.sendAbilitiesUpdate();
        }
        super.afterToggle(src, newValue);
    }
}
