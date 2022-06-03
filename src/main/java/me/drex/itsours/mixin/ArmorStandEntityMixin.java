package me.drex.itsours.mixin;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

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
    public void canInteract(PlayerEntity player, Vec3d hitPos, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        Optional<AbstractClaim> claim = ClaimList.INSTANCE.getClaimAt((ServerWorld) this.getEntityWorld(), this.getBlockPos());
        if (claim.isEmpty()) return;

        if (!claim.get().hasPermission(player.getUuid(), PermissionManager.INTERACT_ENTITY, Node.dummy(Registry.ENTITY_TYPE, this.getType()))) {
            player.sendMessage(Text.translatable("text.itsours.action.disallowed.interact_entity").formatted(Formatting.RED));
            cir.setReturnValue(ActionResult.FAIL);
        }
    }

}
