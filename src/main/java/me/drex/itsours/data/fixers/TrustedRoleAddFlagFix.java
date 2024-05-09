package me.drex.itsours.data.fixers;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import me.drex.itsours.data.fixers.util.ClaimDataFix;

import java.util.Set;

public class TrustedRoleAddFlagFix extends ClaimDataFix {

    private final Set<String> flags;

    public TrustedRoleAddFlagFix(Schema outputSchema, String name, Set<String> flags) {
        super(outputSchema, false, name);
        this.flags = flags;
    }

    @Override
    protected Dynamic<?> fixClaimData(Dynamic<?> claim) {
        return claim.update("groups", groups -> groups.updateMapValues(originalPair -> {
            String role = originalPair.getFirst().asString("");
            if (!role.equals("trusted") && !role.equals("moderator")) return originalPair;
            return originalPair.mapSecond(group -> group.update("flags", this::addFlags));
        }));
    }

    private <T> Dynamic<T> addFlags(Dynamic<T> flags) {
        ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
        // Add flags
        this.flags.forEach(flag -> builder.put(flags.createString(flag), flags.createBoolean(true)));
        builder.putAll(flags.getMapValues().result().orElse(ImmutableMap.of()));
        return flags.createMap(builder.buildKeepingLast());
    }

}
