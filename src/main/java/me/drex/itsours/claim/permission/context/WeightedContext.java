package me.drex.itsours.claim.permission.context;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface WeightedContext extends Comparable<WeightedContext> {

    long SETTING = 1;
    long ROLE = 2;
    long PERMISSION = 3;
    long OWNER = 4;
    long IGNORE = Long.MAX_VALUE;

    long getWeight();

    Text toText();

    @Override
    default int compareTo(@NotNull WeightedContext other) {
        return Long.compare(this.getWeight(), other.getWeight());
    }
}
