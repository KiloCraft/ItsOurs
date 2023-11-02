package me.drex.itsours.data.fixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import me.drex.itsours.data.ItsOursTypeReferences;

import java.util.Collections;

public class AddDefaultPermissionsFix extends DataFix {

    public AddDefaultPermissionsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("Add default_settings", this.getInputSchema().getType(ItsOursTypeReferences.ROOT), typed ->
            typed.update(DSL.remainderFinder(), dynamic ->
                dynamic.set("default_settings", dynamic.createMap(Collections.emptyMap()))
            ));
    }
}
