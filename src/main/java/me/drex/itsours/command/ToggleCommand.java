package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.PlayerData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.drex.message.api.LocalizedMessage.localized;

public class ToggleCommand extends AbstractCommand {

    public static final ToggleCommand SELECT = new ToggleCommand("select", PlayerData::select, PlayerData::setSelect, "text.itsours.commands.select");
    private final BiConsumer<PlayerData, Boolean> dataSetter;
    private final String translationId;
    private Function<PlayerData, Boolean> dataGetter;

    public ToggleCommand(@NotNull String literal, Function<PlayerData, Boolean> dataGetter, BiConsumer<PlayerData, Boolean> dataSetter, String translationId) {
        super(literal);
        this.dataGetter = dataGetter;
        this.dataSetter = dataSetter;
        this.translationId = translationId;
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.executes(ctx -> executeToggle(ctx.getSource()));
    }

    private int executeToggle(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayerOrThrow();
        PlayerData userData = DataManager.getUserData(player.getUuid());
        boolean newValue = !dataGetter.apply(userData);
        dataSetter.accept(userData, newValue);
        if (newValue) {
            src.sendFeedback(() -> localized(translationId + ".enabled"), false);
        } else {
            src.sendFeedback(() -> localized(translationId + ".disabled"), false);
        }
        afterToggle(src, newValue);
        return 1;
    }

    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
    }
}
