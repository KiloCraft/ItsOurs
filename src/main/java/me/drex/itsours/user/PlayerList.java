package me.drex.itsours.user;

import net.minecraft.nbt.NbtCompound;

import java.util.HashMap;
import java.util.UUID;

public class PlayerList extends HashMap<UUID, NbtCompound> {

    private static final HashMap<UUID, NbtCompound> data = new HashMap<>();

    public static <K> void set(UUID uuid, Setting<K> setting, K value) {
        NbtCompound nbtCompound = data.getOrDefault(uuid, new NbtCompound());
        setting.writeNbt(nbtCompound, value);
        if (nbtCompound.getSize() > 0) data.put(uuid, nbtCompound);
    }

    public static <K> K get(UUID uuid, Setting<K> setting) {
        NbtCompound nbtCompound = data.getOrDefault(uuid, new NbtCompound());
       return setting.readNbt(nbtCompound);
    }

    public static void fromNBT(NbtCompound tag) {
        for (String key : tag.getKeys()) {
            NbtCompound nbtCompound = tag.getCompound(key);
            data.put(UUID.fromString(key), nbtCompound);
        }
    }

    public static NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        for (Entry<UUID, NbtCompound> entry : data.entrySet()) {
            if (entry.getValue().getSize() > 0) tag.put(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

}
