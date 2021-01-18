package me.drex.itsours.claim.permission.util.context;

import me.drex.itsours.claim.permission.Permission;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class PermissionContext extends SimpleContext {

    private final Date time = new Date();
    private UUID uuid = new UUID(0, 0);
    private Optional<ServerWorld> world = Optional.empty();

    public PermissionContext() {
        super();
    }

    public UUID getUuid() {
        return uuid;
    }

    public PermissionContext update(SimpleContext context) {
        this.setValue(context.getValue());
        this.setReason(context.getReason());
        return this;
    }

    public PermissionContext setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }

    public PermissionContext setReason(Reason reason) {
        this.reason = reason;
        return this;
    }

    public PermissionContext setValue(Permission.Value value) {
        this.value = value;
        return this;
    }

    public Date getTime() {
        return time;
    }

    public Optional<ServerWorld> getWorld() {
        return world;
    }

    public PermissionContext setWorld(@NotNull ServerWorld world) {
        this.world = Optional.of(world);
        return this;
    }


}
