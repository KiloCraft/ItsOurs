package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.flags.FlagsManager;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.message.api.LocalizedMessage.localized;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin extends LivingEntity {

    protected ArmorStandEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
        method = "interactAt",
        at = @At("HEAD"),
        cancellable = true
    )
    public void itsours$canInteract(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.getEntityWorld(), this.getBlockPos());
        if (claim.isEmpty()) return;

        if (!claim.get().checkAction(player.getUuid(), FlagsManager.INTERACT_ENTITY, Node.registry(Registries.ENTITY_TYPE, this.getType()))) {
            player.sendMessage(localized("text.itsours.action.disallowed.interact_entity"), true);
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
