package me.drex.itsours.data.fixers;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import me.drex.itsours.data.fixers.util.ClaimDataFix;

public class GreatRenameFix extends ClaimDataFix {

    public GreatRenameFix(Schema outputSchema) {
        super(outputSchema, false, "Rename claim fields");
    }

    @Override
    protected Dynamic<?> updateRoot(Dynamic<?> root) {
        root = root.renameField("default_settings", "default_flags");
        return super.updateRoot(root);
    }

    @Override
    protected Dynamic<?> fixClaimData(Dynamic<?> claim) {
        claim = claim.renameField("settings", "flags");
        claim = claim.renameField("permissions", "player_flags");
        claim = claim.update("roles", roles -> roles.updateMapValues(originalPair -> originalPair.mapSecond(group -> group.renameField("permissions", "flags"))));
        claim = claim.renameField("roles", "groups");
        return claim;
    }

}
