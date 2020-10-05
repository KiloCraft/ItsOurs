package me.drex.itsours.claim.permission.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map.Entry;

public class PermissionMap extends HashMap<String, Boolean> {

    public PermissionMap(CompoundTag tag) {
        fromNBT(tag);
    }

    public void fromNBT(CompoundTag tag) {
        tag.getKeys().forEach(permission -> this.put(permission, tag.getBoolean(permission)));
    }

    public CompoundTag toNBT() {
        CompoundTag tag = new CompoundTag();
        this.forEach(tag::putBoolean);
        return tag;
    }

    public boolean getPermission(String permission) {
        return this.get(permission);
    }

    public void setPermission(String permission, boolean value) {
        this.put(permission, value);
    }

    public void resetPermission(String permission) {
        this.remove(permission);
    }

    public boolean isPermissionSet(String permission) {
        return this.containsKey(permission);
    }

    public Text toText() {
        boolean color = false;
        MutableText text = new LiteralText("");
        for (Entry<String, Boolean> entry : this.entrySet()) {
            text.append(new LiteralText(entry.getKey()).formatted(entry.getValue() ? color ? Formatting.DARK_GREEN : Formatting.GREEN : color ? Formatting.DARK_RED : Formatting.RED)).append(" ");
            color = !color;
        }
        return text;
    }

}
