package me.drex.itsours.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import me.drex.itsours.data.fixers.ClaimPositionDataFix;
import me.drex.itsours.data.fixers.PermissionDataFix;
import me.drex.itsours.data.schema.Schema0;

import java.util.function.BiFunction;

import static me.drex.itsours.data.DataManager.CURRENT_DATA_VERSION;

public class ItsOursSchemas {

    public static final DataFixer FIXER = create();
    private static final BiFunction<Integer, Schema, Schema> EMPTY = Schema::new;

    private static synchronized DataFixer create() {
        DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(CURRENT_DATA_VERSION);
        build(dataFixerBuilder);
        return dataFixerBuilder.buildUnoptimized();
    }

    private static void build(DataFixerBuilder builder) {
        Schema schema3 = builder.addSchema(3, Schema0::new);
        builder.addFixer(new ClaimPositionDataFix(schema3));
        builder.addFixer(new PermissionDataFix(schema3));
    }

}
