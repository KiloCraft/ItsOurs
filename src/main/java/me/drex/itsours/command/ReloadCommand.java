package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.drex.itsours.ItsOurs;
import me.drex.message.api.MessageAPI;
import net.minecraft.server.command.ServerCommandSource;

import static me.drex.message.api.LocalizedMessage.localized;

public class ReloadCommand extends AbstractCommand {

    public static final ReloadCommand INSTANCE = new ReloadCommand();

    public ReloadCommand() {
        super("reload");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.executes(context -> {
            MessageAPI.reload();
            context.getSource().sendFeedback(() -> localized("text.itsours.commands.reload"), false);
            return 1;
        }).requires(src -> ItsOurs.checkPermission(src, "itsours.reload", 2));
    }
}
