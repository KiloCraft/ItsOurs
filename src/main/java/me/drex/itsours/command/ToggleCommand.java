package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.user.PlayerData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

import static me.drex.itsours.util.LocalizedMessage.localized;

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
    protected void register(LiteralArgumentBuilder<CommandSourceStack> literal) {
        literal.executes(ctx -> executeToggle(ctx.getSource()));
    }

    private int executeToggle(CommandSourceStack src) throws CommandSyntaxException {
        ServerPlayer player = src.getPlayerOrException();
        PlayerData userData = DataManager.updateUserData(player.getUUID());
        boolean newValue = !dataGetter.apply(userData);
        dataSetter.accept(userData, newValue);
        if (newValue) {
            src.sendSuccess(() -> localized(translationId + ".enabled"), false);
        } else {
            src.sendSuccess(() -> localized(translationId + ".disabled"), false);
        }
        afterToggle(src, newValue);
        return 1;
    }

    protected void afterToggle(CommandSourceStack src, boolean newValue) throws CommandSyntaxException {
    }
}
