package me.drex.itsours.data.fixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import me.drex.itsours.data.fixers.util.ClaimDataFix;

import java.util.Map;
import java.util.Optional;

public class RenameFlagsFix extends ClaimDataFix {

    private final Map<String, String> mappings;

    public RenameFlagsFix(Schema outputSchema, String name, Map<String, String> mappings) {
        super(outputSchema, false, name);
        this.mappings = mappings;
    }

    @Override
    protected Dynamic<?> updateRoot(Dynamic<?> root) {
        root = root.update("default_flags", this::renameFlags);
        return super.updateRoot(root);
    }

    @Override
    protected Dynamic<?> fixClaimData(Dynamic<?> claim) {
        claim = claim.update("flags", this::renameFlags);
        claim = claim.update("player_flags", playerFlags -> playerFlags.updateMapValues(originalPair -> originalPair.mapSecond(this::renameFlags)));
        claim = claim.update("groups", groups -> groups.updateMapValues(originalPair -> originalPair.mapSecond(group -> group.update("flags", this::renameFlags))));
        return claim;
    }

    protected Dynamic<?> renameFlags(Dynamic<?> flags) {
        return flags.updateMapValues(originalPair -> originalPair.mapFirst(flag -> {
            Optional<String> optional = flag.asString().result();
            if (optional.isPresent() && mappings.containsKey(optional.get())) {
                return flag.createString(mappings.get(optional.get()));
            }
            return flag;
        }));
    }
}
