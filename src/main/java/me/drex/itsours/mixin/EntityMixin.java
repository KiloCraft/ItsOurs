package me.drex.itsours.mixin;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.WorldUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Entity.class)
public abstract class EntityMixin {

    private Optional<AbstractClaim> pclaim = Optional.empty();

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
            Optional<AbstractClaim> claim = ItsOursMod.INSTANCE.getClaimList().get((ServerWorld) player.world, player.getBlockPos());
            if (pclaim != claim && player instanceof ServerPlayerEntity) {
                if (player.networkHandler != null) {
                    ClaimPlayer claimPlayer = (ClaimPlayer) player;
                    Optional<Text> message = Optional.empty();
                    if (pclaim.isPresent() && !claim.isPresent()) {
                        message = Optional.of(new LiteralText("You left " + pclaim.get().getFullName()).formatted(Formatting.YELLOW));
                        //TODO: Make configurable
                        boolean cachedFlying = player.getAbilities().flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.getAbilities());
                        //check if the player was flying before they entered the claim
                        if ((boolean) claimPlayer.getSetting("cachedFlight", false)) {
                            player.getAbilities().flying = cachedFlying;
                            player.getAbilities().allowFlying = true;
                        }
                        if (cachedFlying && !player.getAbilities().flying) {
                            BlockPos pos = getPosOnGround(player.getBlockPos(), player.getServerWorld());
                            if (pos.getY() + 3 < player.getY())
                                player.teleport((ServerWorld) WorldUtil.DEFAULT_WORLD, player.getX(), pos.getY(), player.getZ(), player.getYaw(), player.getPitch());
                        }
                        player.sendAbilitiesUpdate();
                    } else if (claim.isPresent()) {
                        if (!pclaim.isPresent()) claimPlayer.setSetting("cachedFlight", player.getAbilities().allowFlying);
                        boolean cachedFlying = player.getAbilities().flying;
                        //update abilities for respective gamemode
                        player.interactionManager.getGameMode().setAbilities(player.getAbilities());
                        //enable flying if player enabled it
                        if (!player.getAbilities().allowFlying) player.getAbilities().allowFlying = (boolean) claimPlayer.getSetting("flight", false);
                        //set the flight state to what it was before entering
                        if (player.getAbilities().allowFlying) player.getAbilities().flying = cachedFlying;
                        player.sendAbilitiesUpdate();
                        message = Optional.of(new LiteralText("Welcome to " + claim.get().getFullName()).formatted(Formatting.YELLOW));
                    }

                    //message.ifPresent(text -> player.networkHandler.sendPacket(new TitleS2CPacket(TitleS2CPacket.Action.ACTIONBAR, text, -1, 20, -1)));
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
        if (!claim.get().hasPermission(playerEntity.getUuid(), "interact_block." + Registry.BLOCK.getId(blockState.getBlock()).getPath())) {
            ClaimPlayer claimPlayer = (ClaimPlayer) playerEntity;
            claimPlayer.sendError(Component.text("You can't interact with that block here.").color(Color.RED));
            return;
        }
        blockState.onEntityCollision(world, pos, entity);
    }

    @Inject(method = "writeNbt", at = @At(value = "HEAD"))
    public void itsours$onEntityToTag(NbtCompound tag, CallbackInfoReturnable<NbtCompound> cir) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ItsOursMod.INSTANCE.getPlayerList().put(player.getUuid(), ((ClaimPlayer)player).toNBT());
        }
    }

    @Inject(method = "readNbt", at = @At(value = "RETURN"))
    public void itsours$onEntityFromTag(NbtCompound tag, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
            ((ClaimPlayer)player).fromNBT(ItsOursMod.INSTANCE.getPlayerList().getTags(player.getUuid()));
        }
    }


}
