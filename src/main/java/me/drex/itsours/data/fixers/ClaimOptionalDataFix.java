package me.drex.itsours.data.fixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import me.drex.itsours.data.ItsOursTypeReferences;

import java.util.Map;
import java.util.stream.Stream;

public class ClaimOptionalDataFix extends DataFix {

    public ClaimOptionalDataFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("Migrate optional claim data to non optional", this.getInputSchema().getType(ItsOursTypeReferences.ROOT), typed ->
            typed.update(DSL.remainderFinder(), dynamic ->
                dynamic.update("claims", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData)))
            )
        );
    }

    private Dynamic<?> fixClaimData(Dynamic<?> claim) {
        claim = claim.update("subzones", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData)));
        if (claim.get("subzones").result().isEmpty()) {
            claim = claim.set("subzones", claim.createList(Stream.of()));
        }
        if (claim.get("settings").result().isEmpty()) {
            claim = claim.set("settings", claim.createMap(Map.of()));
        }
        if (claim.get("permissions").result().isEmpty()) {
            claim = claim.set("permissions", claim.createMap(Map.of()));
        }
        claim = claim.set("messages", claim.createMap(Map.of()));
        return claim;
    }
}
