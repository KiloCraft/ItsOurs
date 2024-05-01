package me.drex.itsours.mixin.tracking;

import com.llamalad7.mixinextras.sugar.Local;
import me.drex.itsours.user.ClaimTrackingPlayer;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataSender.class)
public abstract class ChunkDataSenderMixin {

    @Inject(
        method = "sendChunkBatches",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            ordinal = 0
        )
    )
    private void itsours$onChunkLoad(ServerPlayerEntity player, CallbackInfo ci, @Local List<WorldChunk> chunks) {
        ((ClaimTrackingPlayer) player).addChunkBatch(chunks);
    }

    @Inject(
        method = "unload",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V",
            shift = At.Shift.AFTER
        )
    )
    private void itsours$onChunkUnload(ServerPlayerEntity player, ChunkPos pos, CallbackInfo ci) {
        ((ClaimTrackingPlayer) player).onChunkUnload(pos);
    }

}
