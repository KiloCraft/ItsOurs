package me.drex.itsours.claim.util;

import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockManager {

    private Map<UUID, Integer> blocks = new HashMap<>();

    public BlockManager(CompoundTag tag) {
        this.fromNbt(tag);
    }

    private void fromNbt(CompoundTag tag) {
        tag.getKeys().forEach(key -> blocks.put(UUID.fromString(key), tag.getInt(key)));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        blocks.forEach((uuid, integer) -> tag.putInt(uuid.toString(), integer));
        return tag;
    }

}
