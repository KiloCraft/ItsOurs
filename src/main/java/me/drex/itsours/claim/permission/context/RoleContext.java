package me.drex.itsours.claim.permission.context;

import me.drex.itsours.claim.permission.roles.Role;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record RoleContext(Role role) implements WeightedContext {

    @Override
    public long getWeight() {
        return ROLE;
    }

    @Override
    public Text toText() {
        return Text.translatable("text.itsours.permission.context.role", role.getId()).styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.personal.hover")))
        );
    }

    @Override
    public int compareTo(@NotNull WeightedContext other) {
        if (other instanceof RoleContext roleContext) {
            return this.role.compareTo(roleContext.role);
        }
        return WeightedContext.super.compareTo(other);
    }
}
