package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.block.BlockState;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(
            method = "onLandedUpon",
            at = @At("HEAD"),
            cancellable = true
    )
    private void canPlayerTrample(World world, BlockState blockState, BlockPos pos, Entity entity, float f, CallbackInfo ci) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) world, pos);
        if (claim.isPresent() && entity instanceof ServerPlayerEntity && !claim.get().hasPermission(entity.getUuid(), PermissionManager.MINE, Node.dummy(Registry.BLOCK, (FarmlandBlock) (Object) this))) {
            entity.sendMessage(Text.translatable("text.itsours.action.disallowed.break_block").formatted(Formatting.RED));
            ci.cancel();
        }
    }

}
