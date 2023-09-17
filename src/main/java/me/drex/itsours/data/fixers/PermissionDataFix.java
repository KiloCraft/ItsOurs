package me.drex.itsours.data.fixers;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import me.drex.itsours.data.ItsOursTypeReferences;
import net.minecraft.util.Uuids;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class PermissionDataFix extends DataFix {

    public PermissionDataFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("Fix claim permission data structure", this.getInputSchema().getType(ItsOursTypeReferences.ROOT), typed ->
            typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("claims", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData))))
        );
    }

    private Dynamic<?> fixClaimData(Dynamic<?> claim) {
        OptionalDynamic<?> permissions = claim.get("permissions");
        claim = claim.remove("permissions");
        claim = claim.set("settings", permissions.get("settings").orElseEmptyMap());
        Map<UUID, ? extends Dynamic<?>> players = permissions.get("players").asMap(
            dynamic -> UUID.fromString(dynamic.asString("")),
            Function.identity());

        Map<Dynamic<?>, Dynamic<?>> updatedPermissions = new HashMap<>();
        List<UUID> trustedPlayers = new ArrayList<>();
        players.forEach((uuid, dynamic) -> {
            List<String> roles = dynamic.get("role").asList(dynamic1 -> dynamic1.asString(""));
            if (roles.contains("trusted")) {
                trustedPlayers.add(uuid);
            }
            updatedPermissions.put(dynamic.createString(uuid.toString()), dynamic.get("permission").orElseEmptyMap());
        });
        if (!trustedPlayers.isEmpty()) {
            Dynamic<?> finalClaim = claim;
            Dynamic<?> trustedPlayersDynamic = claim.createList(trustedPlayers.stream().map(Uuids::toIntArray).map(ints -> finalClaim.createIntList(IntStream.of(ints))));
            claim = claim.set("roles", claim.createMap(Map.of(
                claim.createString("trusted"),
                claim.createMap(Map.of(
                    claim.createString("players"),
                    trustedPlayersDynamic,
                    claim.createString("permissions"),
                    claim.createMap(Map.of(
                        claim.createString("use_item"), claim.createBoolean(true),
                        claim.createString("mine"), claim.createBoolean(true),
                        claim.createString("interact_entity"), claim.createBoolean(true),
                        claim.createString("use_on_block"), claim.createBoolean(true),
                        claim.createString("place"), claim.createBoolean(true),
                        claim.createString("damage_entity"), claim.createBoolean(true),
                        claim.createString("interact_block"), claim.createBoolean(true),
                        claim.createString("misc"), claim.createBoolean(true)
                    ))
                )),
                claim.createString("moderator"),
                claim.createMap(Map.of(
                    claim.createString("permissions"),
                    claim.createMap(Map.of(
                        claim.createString("use_item"), claim.createBoolean(true),
                        claim.createString("mine"), claim.createBoolean(true),
                        claim.createString("interact_entity"), claim.createBoolean(true),
                        claim.createString("use_on_block"), claim.createBoolean(true),
                        claim.createString("place"), claim.createBoolean(true),
                        claim.createString("damage_entity"), claim.createBoolean(true),
                        claim.createString("interact_block"), claim.createBoolean(true),
                        claim.createString("misc"), claim.createBoolean(true),
                        claim.createString("modify"), claim.createBoolean(true)
                    ))
                ))
            )));
        }
        claim = claim.set("permissions", claim.createMap(updatedPermissions));
        claim = claim.update("subzones", dynamic1 -> dynamic1.createList(dynamic1.asStream().map(this::fixClaimData)));
        return claim;
    }

}
