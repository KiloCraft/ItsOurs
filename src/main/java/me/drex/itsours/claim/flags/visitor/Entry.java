package me.drex.itsours.claim.flags.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record Entry(AbstractClaim claim,
                    Flag flag,
                    WeightedContext context,
                    Value value) implements Comparable<Entry> {

    public Map<String, Text> placeholders(MinecraftServer server) {
        return PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(server),
            Map.of(
                "flag", Text.literal(flag.asString()),
                "context", context.toText(),
                "value", value.format()
            )
        );
    }

    @Override
    public int compareTo(@NotNull Entry other) {
        // Claim depth
        int claimDepthCompare = Integer.compare(claim.getDepth(), other.claim.getDepth());
        if (claimDepthCompare != 0) return claimDepthCompare;
        // Context
        int contextCompare = context.compareTo(other.context);
        if (contextCompare != 0) return contextCompare;
        // Flag
        if (flag.includes(other.flag)) return -1;
        else if (other.flag.includes(flag)) return 1;
        else return 0;
    }

}
