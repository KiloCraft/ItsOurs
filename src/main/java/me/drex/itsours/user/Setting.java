package me.drex.itsours.user;

import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.List;

public abstract class Setting<K> {

    public static final List<Setting> settings = new ArrayList<>();
    private final String id;
    private final K defaultValue;

    public Setting(String id, K defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
        settings.add(this);
    }

    public String getId() {
        return id;
    }

    public void writeNbt(NbtCompound nbtCompound, K k) {
        if (k.equals(defaultValue)) {
            nbtCompound.remove(id);
        } else {
            toNbt(nbtCompound, k);
        }
    }

    public abstract void toNbt(NbtCompound nbtCompound, K k);

    public abstract K fromNbt(NbtCompound nbtCompound);

    public K readNbt(NbtCompound nbtCompound) {
        return nbtCompound.contains(getId()) ? fromNbt(nbtCompound) : getDefault();
    }

    public K getDefault() {
        return defaultValue;
    }
}
