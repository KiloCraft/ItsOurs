package me.drex.itsours.claim.permission.holder;

import com.mojang.serialization.Codec;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PermissionData {

    public static final Codec<PermissionData> CODEC = Codec.unboundedMap(Permission.CODEC, Codec.BOOL).xmap(PermissionData::new, instance -> instance.data);

    private final Map<Permission, Boolean> data;

    public PermissionData(Map<Permission, Boolean> data) {
        this.data = new HashMap<>(data);
    }

    public PermissionData() {
        this(new HashMap<>());
    }

    public void visit(AbstractClaim claim, Permission permission, WeightedContext context, PermissionVisitor visitor) {
        for (Map.Entry<Permission, Boolean> entry : data.entrySet()) {
            if (entry.getKey().includes(permission)) {
                visitor.visit(claim, entry.getKey(), context, Value.of(entry.getValue()));
            }
        }
    }

    public void set(Permission permission, Value value) {
        if (value == Value.UNSET) {
            data.remove(permission);
        } else {
            data.put(permission, value.value);
        }
    }

    public Value get(Permission permission) {
        Boolean bool = data.get(permission);
        if (bool != null) {
            return Value.of(bool);
        } else {
            return Value.UNSET;
        }
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }

    public Map<String, Text> placeholders() {
        return Map.of(
            "permissions", PlaceholderUtil.list(data.entrySet(), permissionBooleanEntry ->
                    Map.of("permission", Text.literal(permissionBooleanEntry.getKey().asString()).formatted(permissionBooleanEntry.getValue() ? Formatting.GREEN : Formatting.RED)),
                "text.itsours.placeholders.permissions"),
            "permissions_short", PlaceholderUtil.list(data.entrySet().stream().limit(5).collect(Collectors.toSet()), permissionBooleanEntry ->
                    Map.of("permission", Text.literal(permissionBooleanEntry.getKey().asString()).formatted(permissionBooleanEntry.getValue() ? Formatting.GREEN : Formatting.RED)),
                "text.itsours.placeholders.permissions")
        );
    }

    public PermissionData copy() {
        return new PermissionData(new HashMap<>(data));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof PermissionData holder) {
            return data.equals(holder.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
