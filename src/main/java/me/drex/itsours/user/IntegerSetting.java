package me.drex.itsours.user;

import net.minecraft.nbt.NbtCompound;

public class IntegerSetting extends Setting<Integer> {

    public IntegerSetting(String id, Integer defaultValue) {
        super(id, defaultValue);
    }

    @Override
    public void toNbt(NbtCompound nbtCompound, Integer integer) {
        nbtCompound.putInt(getId(), integer);
    }

    @Override
    public Integer fromNbt(NbtCompound nbtCompound) {
        return nbtCompound.getInt(getId());
    }
}
