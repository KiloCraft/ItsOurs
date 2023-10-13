package me.drex.itsours.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.function.Function;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.text.Text.literal;

public class PlaceholderUtil {

    public static Map<String, Text> uuid(String prefix, UUID uuid, MinecraftServer server) {
        return gameProfile(prefix, server.getUserCache().getByUuid(uuid).orElse(new GameProfile(uuid, uuid.toString())));
    }

    public static Map<String, Text> gameProfile(String prefix, GameProfile profile) {
        return Map.of(
            prefix + "name", literal(Objects.requireNonNullElse(profile.getName(), profile.getId().toString())),
            prefix + "uuid", literal(ofNullable(profile.getId()).map(UUID::toString).orElse(profile.getName()))
        );
    }

    public static Map<String, Text> vec3i(String prefix, Vec3i vec3i) {
        return Map.of(
            prefix + "x", literal(valueOf(vec3i.getX())),
            prefix + "y", literal(valueOf(vec3i.getY())),
            prefix + "z", literal(valueOf(vec3i.getZ()))
        );
    }

    public static <T> MutableText list(Collection<T> elements, Function<T, Map<String, Text>> transformer, String messageId) {
        return Texts.join(elements, localized(messageId + ".separator"), t -> localized(messageId + ".entry", transformer.apply(t)));
    }

    @SafeVarargs
    public static Map<String, Text> mergePlaceholderMaps(Map<String, Text>... maps) {
        Map<String, Text> result = new HashMap<>();
        for (Map<String, Text> map : maps) {
            result.putAll(map);
        }
        return result;
    }

}
