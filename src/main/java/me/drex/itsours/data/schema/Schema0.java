package me.drex.itsours.data.schema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import me.drex.itsours.data.ItsOursTypeReferences;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class Schema0 extends Schema {

    public Schema0(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        // Register our custom type reference
        schema.registerType(true, ItsOursTypeReferences.ROOT, DSL::remainder);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        return Collections.emptyMap();
    }


}
