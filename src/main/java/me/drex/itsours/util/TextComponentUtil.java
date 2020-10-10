package me.drex.itsours.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;

public class TextComponentUtil {

    public static Text from(final Component component) {
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

    public static Text error(String error) {
        return from(of(error).color(Color.RED));
    }

    public static Component toComponent(final Text text) {
        return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text));
    }

    public static Component of(final String raw) {
        return of(raw, true);
    }

    public static Component of(final String raw, final boolean markdown) {
        if (markdown) {
            return MiniMessage.markdown().parse(raw);
        }

        return MiniMessage.get().parse(raw);
    }

}