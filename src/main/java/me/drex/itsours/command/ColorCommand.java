package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.server.command.ServerCommandSource;

public class ColorCommand extends Command {

    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("colors");
        command.executes(ctx -> showColors(ctx.getSource()));
        command.requires(src -> hasPermission(src, "itsours.color"));
        literal.then(command);
    }

    public static int showColors(ServerCommandSource source) throws CommandSyntaxException {
        TextComponent.Builder textComponent = Component.text().content("ItsOurs Color Palette:\n\n").color(Color.GRAY);
        for (Color color : Color.COLORS) {
            textComponent.append(Component.text(color.name.toUpperCase() + " ").color(color));
        }
        ((ClaimPlayer)source.getPlayer()).sendMessage(textComponent.build());
        return 1;
    }

}
