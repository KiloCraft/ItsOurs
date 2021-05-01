package me.drex.itsours.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    //TODO:
    /*@Redirect(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    public ActionResult itsours$onInteractAtEntity(Entity entity, PlayerEntity player, Vec3d vec3d, Hand hand) {
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) entity.getEntityWorld(), entity.getBlockPos());
        if (!claim.isPresent())
            return entity.interactAt(player, vec3d, hand);
        if (!claim.get().hasPermission(player.getUuid(), "interact_entity." + Registry.ENTITY_TYPE.getId(entity.getType()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) player;
            claimPlayer.sendError(Component.text("You can't interact with that entity here.").color(Color.RED));
            return ActionResult.FAIL;
        }
        return entity.interactAt(player, vec3d, hand);
    }*/
}
