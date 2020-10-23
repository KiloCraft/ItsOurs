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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    private AbstractClaim pclaim = null;

    @Inject(method = "setPos", at = @At("HEAD"))
    public void doPrePosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            pclaim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
        }
    }

    @Inject(method = "setPos", at = @At("RETURN"))
    public void doPostPosActions(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            if (player.getBlockPos() == null) return;
            AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            if (pclaim != claim && player instanceof ServerPlayerEntity) {
                if (player.networkHandler != null) {
                    ClaimPlayer claimPlayer = (ClaimPlayer) player;
                    Text message = null;
                    if (pclaim != null && claim == null) {
                        message = new LiteralText("You left " + pclaim.getFullName()).formatted(Formatting.YELLOW);
                        //TODO: Make configurable
                        boolean cachedFlying = player.abilities.flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.abilities);
                        //check if the player was flying before they entered the claim
                        if ((boolean) claimPlayer.getSetting("cachedFlight", false)) {
                            player.abilities.flying = cachedFlying;
                            player.abilities.allowFlying = true;
                        }
                        if (cachedFlying && !player.abilities.flying) {
                            BlockPos pos = getPosOnGround(player.getBlockPos(), player.getServerWorld());
                            if (pos.getY() + 3 < player.getY())
                                player.teleport((ServerWorld) WorldUtil.DEFAULT_WORLD, player.getX(), pos.getY(), player.getZ(), player.yaw, player.pitch);
                        }
                        player.sendAbilitiesUpdate();
                    } else if (claim != null) {
                        if (pclaim == null) claimPlayer.setSetting("cachedFlight", player.abilities.allowFlying);
                        boolean cachedFlying = player.abilities.flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.abilities);
                        //enable flying if player enabled it
                        if (!player.abilities.allowFlying) player.abilities.allowFlying = (boolean) claimPlayer.getSetting("flight", false);
                        //set the flight state to what it was before entering
                        if (player.abilities.allowFlying) player.abilities.flying = cachedFlying;
                        player.sendAbilitiesUpdate();
                        message = new LiteralText("Welcome to " + claim.getFullName()).formatted(Formatting.YELLOW);
                    }

                    if (message != null) {
                        player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, message, -1, 20, -1));
                    }
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
        AbstractClaim claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) world, pos);
        if (claim == null) {
            blockState.onEntityCollision(world, pos, entity);
            return;
        }
        if (!claim.hasPermission(playerEntity.getUuid(), "interact_block." + Permission.toString(blockState.getBlock()))) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendError(Component.text("You can't interact with that block here.").color(Color.RED));
            return;
        }
        blockState.onEntityCollision(world, pos, entity);
    }

    @Inject(method = "toTag", at = @At(value = "HEAD"))
    public void itsours$onEntityToTag(CompoundTag tag, CallbackInfoReturnable<CompoundTag> cir) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ItsOursMod.INSTANCE.getPlayerList().put(player.getUuid(), ((ClaimPlayer)player).toNBT());
        }
    }

    @Inject(method = "fromTag", at = @At(value = "RETURN"))
    public void itsours$onEntityFromTag(CompoundTag tag, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ((ClaimPlayer)player).fromNBT(ItsOursMod.INSTANCE.getPlayerList().getTags(player.getUuid()));
        }
    }


}
