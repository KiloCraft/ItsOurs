package me.drex.itsours.util;

import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import me.drex.itsours.data.DataManager;
import net.minecraft.util.Identifier;

import static me.drex.itsours.ItsOurs.MOD_ID;

public class ItsOursPlaceholders {

    private static Identifier location(String placeholderId) {
        return new Identifier(MOD_ID, placeholderId);
    }

    public static void register() {
        Placeholders.register(location("blocks"), (context, argument) -> {
            if (context.hasPlayer()) {
                return PlaceholderResult.value(String.valueOf(DataManager.getUserData(context.player().getUuid()).blocks()));
            } else {
                return PlaceholderResult.invalid("No player!");
            }
        });
    }

}
