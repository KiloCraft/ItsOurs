package me.drex.itsours.claim.roles;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.permission.holder.PermissionData;
import net.minecraft.util.Uuids;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Role {

    public static final Codec<Role> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        PermissionData.CODEC.optionalFieldOf("permissions", new PermissionData()).forGetter(Role::permissions),
        Uuids.INT_STREAM_CODEC.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("players", new HashSet<>()).forGetter(Role::players)
    ).apply(instance, Role::new));

    private final PermissionData permissions;
    private final Set<UUID> players;

    public Role(PermissionData permissions, Set<UUID> players) {
        this.permissions = permissions;
        this.players = new HashSet<>(players);
    }

    public Role() {
        this(new PermissionData(), new HashSet<>());
    }

    public PermissionData permissions() {
        return permissions;
    }

    public Set<UUID> players() {
        return players;
    }
}
