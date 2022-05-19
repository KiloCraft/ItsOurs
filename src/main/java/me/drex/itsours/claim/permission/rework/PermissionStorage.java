package me.drex.itsours.claim.permission.rework;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.rework.context.WeightedContext;
import me.drex.itsours.claim.permission.util.node.util.InvalidPermissionException;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PermissionStorage {

    private static final Logger LOGGER = ItsOurs.LOGGER;
    private final Map<PermissionInterface, Boolean> storage = new HashMap<>();

    private PermissionStorage() {
    }

    public static PermissionStorage fromNbt(NbtCompound nbtCompound) {
        PermissionStorage storage = new PermissionStorage();
        storage.load(nbtCompound);
        return storage;
    }

    public static PermissionStorage storage() {
        return new PermissionStorage();
    }

    public void visit(AbstractClaim claim, PermissionInterface permission, WeightedContext context, PermissionVisitor visitor) {
        for (Map.Entry<PermissionInterface, Boolean> entry : storage.entrySet()) {
            if (entry.getKey().includes(permission)) {
                visitor.visit(claim, entry.getKey(), context, Value.of(entry.getValue()));
            }
        }
    }

    public void load(NbtCompound compound) {
        for (String key : compound.getKeys()) {
            try {
                PermissionRework permission = PermissionRework.of(key);
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

    public void set(PermissionInterface permission, Value value) {
        if (value == Value.UNSET) {
            storage.remove(permission);
        } else {
            storage.put(permission, value.value);
        }
    }

    public Value get(PermissionInterface permission) {
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
        // TODO:
        return Texts.join(storage.entrySet().stream().toList(), entry -> Text.literal(entry.getKey().asString()).formatted(Value.of(entry.getValue()).formatting));
    }

}
