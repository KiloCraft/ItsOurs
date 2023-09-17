package me.drex.itsours.data.fixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import me.drex.itsours.data.ItsOursTypeReferences;

import java.util.Arrays;

public class ClaimPositionDataFix extends DataFix {

    public ClaimPositionDataFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("Fix claim position data structure", this.getInputSchema().getType(ItsOursTypeReferences.ROOT), typed ->
            typed.update(DSL.remainderFinder(), dynamic ->
                dynamic.update("claims", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData)))
            )
        );
    }

    private Dynamic<?> fixClaimData(Dynamic<?> claim) {
        OptionalDynamic<?> position = claim.get("position");
        String world = position.get("world").asString("");
        OptionalDynamic<?> min = position.get("min");
        int minX = min.get("x").asInt(0);
        int minY = min.get("y").asInt(0);
        int minZ = min.get("z").asInt(0);
        OptionalDynamic<?> max = position.get("max");
        int maxX = max.get("x").asInt(0);
        int maxY = max.get("y").asInt(0);
        int maxZ = max.get("z").asInt(0);
        claim = claim.remove("position");
        claim = claim.set("box", claim.createIntList(Arrays.stream(new int[]{minX, minY, minZ, maxX, maxY, maxZ})));
        claim = claim.set("dimension", claim.createString(world));
        claim = claim.update("subzones", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData)));
        return claim;
    }
}
