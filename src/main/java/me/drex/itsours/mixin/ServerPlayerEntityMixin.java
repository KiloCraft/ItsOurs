package me.drex.itsours.mixin;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ClaimPlayer {

    @Shadow
    @Final
    public MinecraftServer server;
    public Pair<BlockPos, BlockPos> positions = new Pair<>(null, null);
    private AbstractClaim lastShowClaim;
    private BlockPos lastShowPos;
    private ServerWorld lastShowWorld;
    private int cooldown = 0;
    private boolean select = false;


    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Override
    public void setLastShow(AbstractClaim claim, BlockPos pos, ServerWorld world) {
        this.lastShowClaim = claim;
        this.lastShowPos = pos;
        this.lastShowWorld = world;
    }

    @Override
    public AbstractClaim getLastShowClaim() {
        return this.lastShowClaim;
    }

    @Override
    public BlockPos getLastShowPos() {
        return this.lastShowPos;
    }

    @Override
    public ServerWorld getLastShowWorld() {
        return this.lastShowWorld;
    }

    @Override
    public boolean arePositionsSet() {
        return positions.getLeft() != null && positions.getRight() != null;
    }

    @Override
    public BlockPos getRightPosition() {
        return positions.getRight();
    }

    @Override
    public void setRightPosition(BlockPos pos) {
        positions = new Pair<>(positions.getLeft(), pos);
    }

    @Override
    public BlockPos getLeftPosition() {
        return positions.getLeft();
    }

    @Override
    public void setLeftPosition(BlockPos pos) {
        positions = new Pair<>(pos, positions.getRight());
    }

    @Override
    public boolean getSelecting() {
        return this.select;
    }

    @Override
    public void setSelecting(boolean value) {
        this.select = value;
    }

    @Override
    public void sendError(String error) {
        if (cooldown == 0) {
            this.sendMessage(new LiteralText(error), false);
            //TODO: Make configurable
            cooldown = 20;
        }
    }

    @Override
    public void sendError(Component error) {
        if (cooldown == 0) {
            this.sendMessage(error);
            //TODO: Make configurable
            cooldown = 20;
        }
    }

    @Override
    public void sendMessage(Text message) {
        this.sendMessage(message, false);
    }

    @Override
    public void sendMessage(Component component) {
        this.sendMessage(TextComponentUtil.from(component), false);
    }

    @Override
    public void sendActionbar(Component component) {
        this.sendMessage(TextComponentUtil.from(component), true);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void claimPlayer$onTick(CallbackInfo ci) {
        if (cooldown > 0) cooldown--;
    }

}
