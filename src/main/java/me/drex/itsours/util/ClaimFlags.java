package me.drex.itsours.util;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.flags.node.ChildNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static me.drex.message.api.LocalizedMessage.localized;

public class ClaimFlags {

    public static <T> T check(Entity entity, String localization, Supplier<T> failure, Supplier<T> success, ChildNode... childNodes) {
        return check(entity, entity.getUuid(), entity.getWorld(), entity.getBlockPos(), localization, failure, success, childNodes);
    }

    public static <T> T check(@Nullable Entity entity, @Nullable UUID uuid, World world, BlockPos pos, String localization, Supplier<T> failure, Supplier<T> success, ChildNode... childNodes) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(world, pos);
        if (claim.isPresent() && !claim.get().checkAction(uuid, childNodes)) {
            if (entity instanceof ServerPlayerEntity player) {
                player.sendMessage(localized(localization), true);
            }
            return failure.get();
        }
        return success.get();
    }


}
