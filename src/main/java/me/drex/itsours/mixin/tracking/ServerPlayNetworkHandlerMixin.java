package me.drex.itsours.mixin.tracking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.drex.itsours.user.ClaimTrackingPlayer;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.AcknowledgeChunksC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;

    @WrapOperation(
        method = "onPlayerInteractBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    private void itsours$shouldUpdateBlock(ServerPlayNetworkHandler instance, Packet<?> packet, Operation<Void> original, PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket) {
        if (!((ClaimTrackingPlayer) player).isTracked(playerInteractBlockC2SPacket.getBlockHitResult().getBlockPos())) {
            original.call(instance, packet);
        }
    }

    @Inject(
        method = "onAcknowledgeChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ChunkDataSender;onAcknowledgeChunks(F)V"
        )
    )
    private void itsours$onAcknowledgeChunks(AcknowledgeChunksC2SPacket packet, CallbackInfo ci) {
        ((ClaimTrackingPlayer) player).batchAcknowledged();
    }

}
