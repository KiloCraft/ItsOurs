package me.drex.itsours.mixin.tracking;

import com.llamalad7.mixinextras.sugar.Local;
import me.drex.itsours.user.ClaimTrackingPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerChunkSender.class)
public abstract class PlayerChunkSenderMixin {

    @Inject(
        method = "sendNextChunks",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            ordinal = 1
        )
    )
    private void itsours$onChunkLoad(ServerPlayer player, CallbackInfo ci, @Local List<LevelChunk> chunks) {
        ((ClaimTrackingPlayer) player).addChunkBatch(chunks);
    }

    @Inject(
        method = "dropChunk",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V",
            shift = At.Shift.AFTER
        )
    )
    private void itsours$onChunkUnload(ServerPlayer player, ChunkPos pos, CallbackInfo ci) {
        ((ClaimTrackingPlayer) player).onChunkUnload(pos);
    }

}
