package me.drex.itsours.data.fixers.util;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import me.drex.itsours.data.ItsOursTypeReferences;

public abstract class ClaimDataFix extends DataFix {

    private final String name;

    public ClaimDataFix(Schema outputSchema, boolean changesType, String name) {
        super(outputSchema, changesType);
        this.name = name;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(ItsOursTypeReferences.ROOT), typed ->
            typed.update(DSL.remainderFinder(), this::updateRoot)
        );
    }

    protected Dynamic<?> updateRoot(Dynamic<?> root) {
        return root.update("claims", claims -> claims.createList(claims.asStream().map(this::updateClaimData)));
    }

    private Dynamic<?> updateClaimData(Dynamic<?> claim) {
        claim = fixClaimData(claim);
        claim = claim.update("subzones", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::updateClaimData)));
        return claim;
    }

    protected abstract Dynamic<?> fixClaimData(Dynamic<?> claim);


}
