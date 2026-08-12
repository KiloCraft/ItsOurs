package me.drex.itsours.util;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.node.ChildNode;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class ClaimFlags {

    public static <T> T check(Entity entity, String localization, Supplier<T> failure, Supplier<T> success, ChildNode... childNodes) {
        return check(entity, entity.getUUID(), entity.level(), entity.blockPosition(), localization, failure, success, childNodes);
    }

    public static <T> T check(@Nullable Entity entity, @Nullable UUID uuid, Level world, BlockPos pos, String localization, Supplier<T> failure, Supplier<T> success, ChildNode... childNodes) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent() && !claim.get().checkAction(uuid, childNodes)) {
            if (entity instanceof ServerPlayer player) {
                player.displayClientMessage(localized(localization, player), true);
            }
            return failure.get();
        }
        return success.get();
    }


}
