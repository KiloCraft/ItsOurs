package me.drex.itsours.claim.permission.context;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public interface WeightedContext extends Comparable<WeightedContext> {

    long DEFAULT = 1;
    long GLOBAL = 2;
    long ROLE = 3;
    long PERSONAL = 4;
    long OWNER = 5;
    long IGNORE = Long.MAX_VALUE;

    long getWeight();

    Text toText();

    @Override
    default int compareTo(@NotNull WeightedContext other) {
        return Long.compare(this.getWeight(), other.getWeight());
    }
}
