package me.drex.itsours.claim.permission.holder;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionImpl;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.claim.permission.visitor.PermissionVisitor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PermissionHolder {

    private static final Logger LOGGER = ItsOurs.LOGGER;
    private final Map<Permission, Boolean> storage = new HashMap<>();

    private PermissionHolder() {
    }

    public static PermissionHolder fromNbt(NbtCompound nbtCompound) {
        PermissionHolder storage = new PermissionHolder();
        storage.load(nbtCompound);
        return storage;
    }

    public static PermissionHolder storage() {
        return new PermissionHolder();
    }

    public void visit(AbstractClaim claim, Permission permission, WeightedContext context, PermissionVisitor visitor) {
        for (Map.Entry<Permission, Boolean> entry : storage.entrySet()) {
            if (entry.getKey().includes(permission)) {
                visitor.visit(claim, entry.getKey(), context, Value.of(entry.getValue()));
            }
        }
    }

    public void load(NbtCompound compound) {
        for (String key : compound.getKeys()) {
            try {
                PermissionImpl permission = PermissionImpl.fromId(key);
                storage.put(permission, compound.getBoolean(key));
            } catch (InvalidPermissionException e) {
                LOGGER.warn("Failed to load invalid permission {}", key);
            }
        }
    }

    public NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        storage.forEach((permission, value) -> {
            compound.putBoolean(permission.asString(), value);
        });
        return compound;
    }

    public void set(Permission permission, Value value) {
        if (value == Value.UNSET) {
            storage.remove(permission);
        } else {
            storage.put(permission, value.value);
        }
    }

    public Value get(Permission permission) {
        Boolean bool = storage.get(permission);
        if (bool != null) {
            return Value.of(bool);
        } else {
            return Value.UNSET;
        }
    }

    public boolean isEmpty() {
        return storage.isEmpty();
    }

    public int size() {
        return storage.size();
    }

    public Text toText() {
        return Texts.join(storage.entrySet().stream().toList(), entry -> Text.literal(entry.getKey().asString()).formatted(Value.of(entry.getValue()).formatting));
    }

}
