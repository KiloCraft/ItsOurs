package me.drex.itsours.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.core.Vec3i;

import java.util.*;
import java.util.function.Function;

import static java.lang.String.valueOf;
import static java.util.Optional.ofNullable;
import static me.drex.itsours.util.LocalizedMessage.localized;
import static net.minecraft.network.chat.Component.literal;

public class PlaceholderUtil {

    public static Map<String, Component> uuid(String prefix, UUID uuid, MinecraftServer server) {
        return gameProfile(prefix, server.services().nameToIdCache().get(uuid).orElse(new NameAndId(uuid, uuid.toString())));
    }

    public static Map<String, Component> gameProfile(String prefix, NameAndId profile) {
        return Map.of(
            prefix + "name", literal(profile.name()),
            prefix + "uuid", literal(profile.id().toString())
        );
    }

    public static Map<String, Component> vec3i(String prefix, Vec3i vec3i) {
        return Map.of(
            prefix + "x", literal(valueOf(vec3i.getX())),
            prefix + "y", literal(valueOf(vec3i.getY())),
            prefix + "z", literal(valueOf(vec3i.getZ()))
        );
    }

    public static <T> MutableComponent list(Collection<T> elements, Function<T, Map<String, Component>> transformer, String messageId) {
        return ComponentUtils.formatList(elements, localized(messageId + ".separator"), t -> localized(messageId + ".entry", transformer.apply(t)));
    }

    @SafeVarargs
    public static Map<String, Component> mergePlaceholderMaps(Map<String, Component>... maps) {
        Map<String, Component> result = new HashMap<>();
        for (Map<String, Component> map : maps) {
            result.putAll(map);
        }
        return result;
    }

}
