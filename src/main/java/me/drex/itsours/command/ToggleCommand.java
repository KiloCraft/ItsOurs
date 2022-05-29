package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.user.Setting;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public abstract class ToggleCommand extends AbstractCommand {

    private final Setting<Boolean> setting;
    private final String translationId;

    public ToggleCommand(@NotNull String literal, Setting<Boolean> setting, String translationId) {
        super(literal);
        this.setting = setting;
        this.translationId = translationId;
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.executes(ctx -> executeToggle(ctx.getSource()));
    }

    private int executeToggle(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();
        boolean newValue = !PlayerList.get(player.getUuid(), setting);
        PlayerList.set(player.getUuid(), setting, newValue);
        if (newValue) {
            src.sendFeedback(Text.translatable(translationId + ".enabled").formatted(Formatting.GREEN), false);
        } else {
            src.sendFeedback(Text.translatable(translationId + ".disabled").formatted(Formatting.RED), false);
        }
        afterToggle(src, newValue);
        return 1;
    }

    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
    }
}
