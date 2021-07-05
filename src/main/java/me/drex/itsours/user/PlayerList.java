package me.drex.itsours.user;

import me.drex.itsours.ItsOursMod;
import net.minecraft.nbt.NbtCompound;
import java.util.HashMap;
import java.util.UUID;

public class PlayerList extends HashMap<UUID, NbtCompound> {

    private static final HashMap<UUID, NbtCompound> data = new HashMap<>();

    public static <K> void set(UUID uuid, Setting<K> setting, K value) {
        NbtCompound nbtCompound = data.getOrDefault(uuid, new NbtCompound());
        setting.writeNbt(nbtCompound, value);
    }

    public static <K> K get(UUID uuid, Setting<K> setting) {
        NbtCompound nbtCompound = data.getOrDefault(uuid, new NbtCompound());
       return setting.readNbt(nbtCompound);
    }

    public static Object get(String key, NbtCompound tag) {
        switch (tag.get(key).getType()) {
            case 1:
                return tag.getBoolean(key);
            case 3:
                return tag.getInt(key);
            default:
                ItsOursMod.LOGGER.error("Illegal type for \"" + key + "\": " + tag.get(key).getType());
                return null;
        }
    }

    public static void fromNBT(NbtCompound tag) {
        for (String key : tag.getKeys()) {
            NbtCompound nbtCompound = tag.getCompound(key);
            NbtCompound copy = nbtCompound.copy();
            for (String s : nbtCompound.getKeys()) {
                boolean keep = false;
                for (Setting<?> setting : Setting.settings) {
                    if (setting.getId().equals(s) && setting.getDefault().equals(setting.fromNbt(nbtCompound))) {
                        keep = true;
                    }
                }
                if (!keep) copy.remove(s);
            }
            if (copy.getSize() > 0) data.put(UUID.fromString(key), copy);
        }
    }

    public static NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        for (Entry<UUID, NbtCompound> entry : data.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

}
