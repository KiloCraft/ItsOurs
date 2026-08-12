package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.Node;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.drex.itsours.util.LocalizedMessage.localized;

@Mixin(ArmorStand.class)
public abstract class ArmorStandMixin extends LivingEntity {

    protected ArmorStandMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(
        method = "interactAt",
        at = @At("HEAD"),
        cancellable = true
    )
    public void itsours$canInteract(Player player, Vec3 hitPos, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Optional<AbstractClaim> claim = ClaimList.getClaimAt(this.level(), this.blockPosition());
        if (claim.isEmpty()) return;

        if (!claim.get().checkAction(player.getUUID(), Flags.INTERACT_ENTITY, Node.registry(BuiltInRegistries.ENTITY_TYPE, this.getType()))) {
            player.displayClientMessage(localized("text.itsours.action.disallowed.interact_entity", player), true);
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

}
