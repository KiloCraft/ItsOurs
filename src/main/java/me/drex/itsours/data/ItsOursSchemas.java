package me.drex.itsours.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import me.drex.itsours.data.fixers.AddDefaultPermissionsFix;
import me.drex.itsours.data.fixers.ClaimOptionalDataFix;
import me.drex.itsours.data.fixers.ClaimPositionDataFix;
import me.drex.itsours.data.fixers.PermissionDataFix;
import me.drex.itsours.data.schema.Schema0;

import java.util.function.BiFunction;

import static me.drex.itsours.data.DataManager.CURRENT_DATA_VERSION;

public class ItsOursSchemas {

    private static final BiFunction<Integer, Schema, Schema> EMPTY = Schema::new;
    public static final DataFixer FIXER = create();

    private static synchronized DataFixer create() {
        DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(CURRENT_DATA_VERSION);
        build(dataFixerBuilder);
        return dataFixerBuilder.buildUnoptimized();
    }

    private static void build(DataFixerBuilder builder) {
        // Claim data has been rewritten to use codecs and had some internal changes
        Schema schema3 = builder.addSchema(3, Schema0::new);
        builder.addFixer(new ClaimPositionDataFix(schema3));
        builder.addFixer(new PermissionDataFix(schema3));
        // default_settings is no longer an optional field and needs to be present
        Schema schema4 = builder.addSchema(4, EMPTY);
        builder.addFixer(new AddDefaultPermissionsFix(schema4));
        Schema schema5 = builder.addSchema(5, EMPTY);
        builder.addFixer(new ClaimOptionalDataFix(schema5));
    }

}
