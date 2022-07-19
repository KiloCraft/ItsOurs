package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(SculkCatalystBlockEntity.class)
public abstract class SculkCatalystBlockEntityMixin extends BlockEntity {

    public SculkCatalystBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Redirect(method = "listen", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/SculkSpreadManager;spread(Lnet/minecraft/util/math/BlockPos;I)V"))
    public void dontSpreadFromOtherSculkCatalyst(SculkSpreadManager sculkSpreadManager, BlockPos pos, int charge) {
        BlockPos oldPos = this.pos;
        Optional<AbstractClaim> oldClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, oldPos);
        Optional<AbstractClaim> newClaim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        if (((oldClaim.isEmpty() || oldClaim.get().hasPermission(null, PermissionManager.SCULK_CROSSES_BORDERS)) &&
                (newClaim.isEmpty() || newClaim.get().hasPermission(null, PermissionManager.SCULK_CROSSES_BORDERS))) || newClaim.equals(oldClaim)) {
            sculkSpreadManager.spread(pos, charge);
        }
    }

}
