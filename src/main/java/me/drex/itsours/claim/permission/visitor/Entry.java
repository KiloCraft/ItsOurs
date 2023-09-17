package me.drex.itsours.claim.permission.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record Entry(AbstractClaim claim,
                    Permission permission,
                    WeightedContext context,
                    Value value) implements Comparable<Entry> {

    public Map<String, Text> placeholders(MinecraftServer server) {
        return PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(server),
            Map.of(
                "permission", Text.literal(permission.asString()),
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
        // Permission
        if (permission.includes(other.permission)) return -1;
        else if (other.permission.includes(permission)) return 1;
        else return 0;
    }

}
