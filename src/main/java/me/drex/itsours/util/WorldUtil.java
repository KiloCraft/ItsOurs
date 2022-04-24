package me.drex.itsours.util;

import com.google.common.collect.Lists;
import me.drex.itsours.ItsOurs;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.List;

public class WorldUtil {

    private static final List<RegistryKey<World>> REGISTRY_KEYS = Lists.newArrayList();

    static {
        REGISTRY_KEYS.addAll(ItsOurs.INSTANCE.server.getWorldRegistryKeys());
    }

    public static ServerWorld getWorld(String identifier) {
        for (RegistryKey<World> key : REGISTRY_KEYS) {
            if (identifier.equals(key.getValue().toString())) {
                return ItsOurs.INSTANCE.server.getWorld(key);
            }
        }
        throw new RuntimeException("Unable to get world: " + identifier);
    }

    public static String toIdentifier(ServerWorld world) {
        for (RegistryKey<World> key : REGISTRY_KEYS) {
            if (world == ItsOurs.INSTANCE.server.getWorld(key)) return key.getValue().toString();
        }
        return "";
    }

    public static boolean isOverworld(ServerWorld world) {
        return world.getRegistryKey().equals(World.OVERWORLD);
    }
    public static boolean isNether(ServerWorld world) {
        return world.getRegistryKey().equals(World.NETHER);
    }
    public static boolean isEnd(ServerWorld world) {
        return world.getRegistryKey().equals(World.END);
    }

}
