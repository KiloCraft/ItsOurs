package me.drex.itsours.claim.permission.rework.context;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public record RoleContext(int roleWeight) implements WeightedContext {

    @Override
    public long getWeight() {
        return ROLE;
    }

    @Override
    public Text toText() {
        // TODO:
        return Text.empty();
    }

    @Override
    public int compareTo(@NotNull WeightedContext other) {
        if (other instanceof RoleContext roleContext) {
            return Integer.compare(roleWeight, roleContext.roleWeight);
        }
        return WeightedContext.super.compareTo(other);
    }
}
