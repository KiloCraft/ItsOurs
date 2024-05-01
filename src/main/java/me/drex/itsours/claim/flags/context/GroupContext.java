package me.drex.itsours.claim.flags.context;

import me.drex.itsours.claim.groups.Group;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.text.Text.literal;

public record GroupContext(String id, int priority, Group group) implements WeightedContext {

    @Override
    public long getWeight() {
        return GROUP;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.flag.context.group", Map.of("group_id", literal(id)));
    }

    @Override
    public int compareTo(@NotNull WeightedContext other) {
        if (other instanceof GroupContext groupContext) {
            return Integer.compare(groupContext.priority, this.priority);
        }
        return WeightedContext.super.compareTo(other);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof GroupContext groupContext) {
            return groupContext.group().equals(group);
        }
        return false;
    }
}
