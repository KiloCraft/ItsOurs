package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.util.Permission;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityMixin {

    protected UUID uuid;
    public Optional<AbstractClaim> pclaim = Optional.empty();
    World pworld;
    BlockPos ppos;

    @Inject(method = "setPos", at = @At("HEAD"))
    public void itsours$doPrePosActions(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            pclaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            pworld = player.world;
            ppos = player.getBlockPos();
        }
    }

    @Inject(method = "setPos", at = @At("RETURN"))
    public void itsours$doPostPosActions(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            if (!pclaim.equals(claim)) {
                //System.out.println("setPos (" + WorldUtil.toIdentifier((ServerWorld) pworld) + ", " + ppos + ") " + pclaim + " -> (" + WorldUtil.toIdentifier((ServerWorld) player.world) + ", " + player.getBlockPos() + ") " + claim);
                if (player.networkHandler != null) {
                    pclaim.ifPresent(c -> c.onLeave(claim, player));
                    claim.ifPresent(c -> c.onEnter(pclaim, player));
                }
            }
        }
    }


    public BlockPos getPosOnGround(BlockPos pos, World world) {
        BlockPos blockPos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        do {
            blockPos = blockPos.down();
            if (blockPos.getY() < 1) {
                return pos;
            }
        } while (world.getBlockState(blockPos).isAir());

        return blockPos.up();
    }

    @Redirect(method = "checkBlockCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"))
    private void itsours$onBlockCollision(BlockState blockState, World world, BlockPos pos, Entity entity) {
        ServerPlayerEntity playerEntity = null;
        if (entity instanceof ProjectileEntity && (blockState.getBlock() instanceof AbstractButtonBlock || blockState.getBlock() instanceof AbstractPressurePlateBlock)) {
            ProjectileEntity projectileEntity = (ProjectileEntity) entity;
            if (projectileEntity.getOwner() != null && projectileEntity.getOwner() instanceof ServerPlayerEntity) {
                playerEntity = (ServerPlayerEntity) projectileEntity.getOwner();
            }
        } else if (entity instanceof ServerPlayerEntity && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            playerEntity = (ServerPlayerEntity) entity;
        } else if (entity instanceof ItemEntity && blockState.getBlock() instanceof AbstractPressurePlateBlock) {
            ItemEntity item = (ItemEntity) entity;
            if (item.getThrower() != null) {
                playerEntity = ItsOursMod.server.getPlayerManager().getPlayer(item.getThrower());
            }
        }
        if (playerEntity == null) {
            blockState.onEntityCollision(world, pos, entity);
            return;
        }
        Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, pos);
        if (!claim.isPresent()) {
            blockState.onEntityCollision(world, pos, entity);
            return;
        }
        if (!claim.get().hasPermission(playerEntity.getUuid(), "interact_block." + Permission.toString(blockState.getBlock()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendError(Component.text("You can't interact with that block here.").color(Color.RED));
            return;
        }
        blockState.onEntityCollision(world, pos, entity);
    }


}
