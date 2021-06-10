package me.drex.itsours.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.command.RemoveCommand;
import me.drex.itsours.command.ShowCommand;
import me.drex.itsours.util.TextComponentUtil;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ClaimInfoScreenHandler extends GUIScreenHandler {

    protected final AbstractClaim claim;

    protected ClaimInfoScreenHandler(int syncId, PlayerEntity player, AbstractClaim claim) {
        super(syncId, 1, player);
        this.claim = claim;
        this.fillInventory(player, this.inventory);

    }

    public static void openMenu(ServerPlayerEntity player, AbstractClaim claim) {
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new ClaimInfoScreenHandler(syncId, player, claim);
            }

            @Override
            public Text getDisplayName() {
                //TODO:
                return new LiteralText("Claim info");
            }
        };
        player.openHandledScreen(factory);
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inventory) {
        ItemStack settings = new ItemStack(Items.REDSTONE_TORCH);
        settings.setCustomName(new LiteralText("Settings").formatted(Formatting.GOLD));
        inventory.setStack(0, settings);

        ItemStack trusted = new ItemStack(Items.PLAYER_HEAD);
        trusted.setCustomName(new LiteralText("TrustManager").formatted(Formatting.DARK_GREEN));
        inventory.setStack(2, trusted);

        BlockPos tpPos = new BlockPos((claim.min.getX() + claim.max.getX()) / 2, (claim.min.getY() + claim.max.getY() / 2), (claim.min.getZ() + claim.max.getZ()) / 2);
        tpPos = AbstractClaim.Util.getPosOnGround(tpPos, claim.getWorld());
        ItemStack location = new ItemStack(Items.COMPASS);
        location.setCustomName(new LiteralText("Location").formatted(Formatting.AQUA));
        ScreenHelper.addLore(location, TextComponentUtil.of("<white>Position: <gray>" + tpPos.getX() + " / " + tpPos.getY() + " / " + tpPos.getZ()));
        ScreenHelper.addLore(location, TextComponentUtil.of("<white>Dimension: <gray>" + WorldUtil.toIdentifier(claim.getWorld())));
        ScreenHelper.addLore(location, TextComponentUtil.of("<white>Area: <gray>" + claim.getArea()));
        inventory.setStack(4, location);

        ItemStack showBorder = new ItemStack(Items.GOLD_BLOCK);
        showBorder.setCustomName(new LiteralText("Show claim borders").formatted(Formatting.YELLOW));
        inventory.setStack(6, showBorder);

        ItemStack close = new ItemStack(Items.EMERALD);
        close.setCustomName(new LiteralText("REMOVE").formatted(Formatting.DARK_RED, Formatting.BOLD));
        inventory.setStack(8, close);
        fillEmpty();
    }

    @Override
    protected void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shift) {
        switch (index) {
            case 0 -> {
                player.closeHandledScreen();
                player.getServer().execute(() -> SettingInfoScreenHandler.openMenu(player, claim.getPermissionManager().settings, this, 0, PermissionList.setting, PermissionList.permission));
            }
            case 2 -> {
                player.closeHandledScreen();
                player.getServer().execute(() -> TrustedScreenHandler.openMenu(player, claim, this, 0));
            }
            case 6 -> {
                try {
                    ShowCommand.show(player.getCommandSource(), true);
                } catch (CommandSyntaxException e) {
                    player.sendMessage(new LiteralText(e.getContext()), false);
                }
            }
            case 8 -> {
                player.closeHandledScreen();
                try {
                    RemoveCommand.requestRemove(player.getCommandSource(), claim);
                } catch (CommandSyntaxException e) {
                    player.sendMessage(new LiteralText(e.getContext()), false);
                }
            }
        }
    }
}
