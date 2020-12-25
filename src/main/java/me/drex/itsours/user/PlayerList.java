package me.drex.itsours.user;

import me.drex.itsours.ItsOursMod;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.UUID;

public class PlayerList extends HashMap<UUID, CompoundTag> {

    public PlayerList(CompoundTag tag) {
        this.fromNBT(tag);
    }

    public static Object get(String key, CompoundTag tag) {
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

    public static void set(String key, CompoundTag tag, Object value) {
        if (value instanceof Boolean) {
            tag.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            tag.putInt(key, (Integer) value);
        }
    }

    public void fromNBT(CompoundTag tag) {
        for (String key : tag.getKeys()) {
            this.put(UUID.fromString(key), tag.getCompound(key));
        }
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        for (Entry<UUID, CompoundTag> entry : this.entrySet()) {
            tag.put(entry.getKey().toString(), entry.getValue());
        }
        return tag;
    }

    public CompoundTag getTags(UUID uuid) {
        return this.containsKey(uuid) ? this.get(uuid) : new CompoundTag();
    }

    private Object get(UUID uuid, String key, Object defaultValue) {
        CompoundTag tag = getTags(uuid);
        if (tag.contains(key)) {
            Object o = get(key, tag);
            return o == null ? defaultValue : o;
        } else {
            return defaultValue;
        }
    }

    public Object get(UUID uuid, PlayerSetting setting) {
        return get(uuid, setting.id, setting.defaultValue);
    }

    public int getBlocks(UUID uuid) {
        return (int) get(uuid, PlayerSetting.BLOCKS);
    }

    public void setBlocks(UUID uuid, int value) {
        set(uuid, "blocks", value);
    }

    public boolean getBoolean(UUID uuid, PlayerSetting setting) {
        return (boolean) get(uuid, setting);
    }

    public void setBoolean(UUID uuid, PlayerSetting setting, boolean value) {
        set(uuid, setting.id, value);
    }

    private void set(UUID uuid, String key, Object value) {
        CompoundTag tag = getTags(uuid);
        set(key, tag, value);
        this.put(uuid, tag);
    }

    public void set(UUID uuid, PlayerSetting setting, Object value) {
        set(uuid, setting.id, value);
    }

}
