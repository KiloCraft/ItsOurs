package me.drex.itsours.user;

import net.minecraft.nbt.NbtCompound;

public class BoolSetting extends Setting<Boolean> {

    public BoolSetting(String id, Boolean defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public void toNbt(NbtCompound nbtCompound, Boolean bool) {
        nbtCompound.putBoolean(getId(), bool);
    }

    @Override
    public Boolean fromNbt(NbtCompound nbtCompound) {
        return nbtCompound.getBoolean(getId());
    }
}
