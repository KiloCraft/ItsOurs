package me.drex.itsours.claim.permission.context;

import me.drex.itsours.claim.roles.Role;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.text.Text.literal;

public record RoleContext(String id, int priority, Role role) implements WeightedContext {

    @Override
    public long getWeight() {
        return ROLE;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.permission.context.role", Map.of("role_id", literal(id)));
    }

    @Override
    public int compareTo(@NotNull WeightedContext other) {
        if (other instanceof RoleContext roleContext) {
            return Integer.compare(roleContext.priority, this.priority);
        }
        return WeightedContext.super.compareTo(other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof RoleContext roleContext) {
            return roleContext.role().equals(role);
        }
        return false;
    }
}
