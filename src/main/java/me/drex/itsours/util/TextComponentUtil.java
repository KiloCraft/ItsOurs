package me.drex.itsours.util;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.ItsOursMod;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public class TextComponentUtil {

    public static Text from(final Component component) {
        return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(component));
    }

    public static Text error(String error) {
        return from(of(error).color(Color.RED));
    }

    public static Text toText(String input) {
        return from(of(input));
    }

    public static Component toComponent(final Text text) {
        return GsonComponentSerializer.gson().deserialize(Text.Serializer.toJson(text));
    }

    public static Component of(final String raw) {
        return of(raw, true);
    }

    public static Component of(final String raw, final boolean markdown) {
        return MiniMessage.miniMessage().parse(raw);
    }

    public static Component toName(UUID uuid, TextColor color) {
        Optional<GameProfile> optional = ItsOursMod.server.getUserCache().getByUuid(uuid);
        Component text;
        if (optional.isPresent() && optional.get().isComplete()) {
            text = Component.text(optional.get().getName());
        } else {
            text = Component.text(uuid.toString()).decorate(TextDecoration.ITALIC).clickEvent(ClickEvent.copyToClipboard(uuid.toString()))
                    .hoverEvent(HoverEvent.showText(Component.text("Click to copy!").color(Color.AQUA)));
        }
        return text.color(color);
    }

}
