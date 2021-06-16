package me.drex.itsours.gui.screen;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.command.TrustCommand;
import me.drex.itsours.command.TrustedCommand;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.ClaimContext;
import me.drex.itsours.gui.util.context.PermissionContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TrustedScreen extends PagedScreen<ClaimContext> {

    public TrustedScreen(ServerPlayerEntity player, int rows, ClaimContext context, SimpleScreen<ClaimContext> previous) {
        super(player, rows, context, previous);
        entriesPerPage = 14;
        ItemStack info = new ItemStack(Items.PAPER);
        ScreenHelper.setCustomName(info, "TrustManager");
        ScreenHelper.addLore(info, "This page allows", "you to change who", "is trusted in", "your claim");
        addSlot(new SlotEntry<>(info), 4);
    }

    @Override
    public void draw() {
        AbstractClaim claim = context.getClaim();
        Set<UUID> allUUIDs = TrustedCommand.getAllUUIDs(claim);
        List<ServerPlayerEntity> players = player.getServerWorld().getPlayers(playerEntity -> playerEntity.squaredDistanceTo(player) < 500);
        for (ServerPlayerEntity entity : players) {
            allUUIDs.add(entity.getUuid());
        }
        allUUIDs.remove(player.getUuid());
        CompletableFuture.runAsync(() -> {
            for (UUID uuid : allUUIDs) {
                Role trusted = ItsOursMod.INSTANCE.getRoleManager().getRole("trusted");
                Permission.Value value;
                if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRemoved().contains(trusted)) {
                    value = Permission.Value.FALSE;
                } else if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRoles().containsKey(trusted)) {
                    value = Permission.Value.TRUE;
                } else {
                    value = Permission.Value.UNSET;
                }
                ItemStack playerHead = ScreenHelper.createPlayerHead(uuid);
                String name = ScreenHelper.toName(uuid);
                ScreenHelper.addLore(playerHead, "Click to open", "permission manager");
                ScreenHelper.setCustomName(playerHead, name);
                SlotEntry<ClaimContext> slotEntry = new SlotEntry<>(playerHead, (claimContext, leftClick, shiftClick) -> {
                    close();
                    PermissionScreen permissionScreen = new PermissionScreen(player, 6,
                            new PermissionContext() {
                                public AbstractClaim getClaim() {
                                    return claimContext.getClaim();
                                }

                                public UUID getUUID() {
                                    return uuid;
                                }
                            },
                            this, PermissionList.permission);
                    permissionScreen.render();
                });
                addPageEntry(slotEntry);
                addToggle(uuid, value);
            }
            super.draw();
        });
    }

    @Override
    public void addPageEntry(SlotEntry<ClaimContext> slotEntry) {
        int i = 0;
        int line = nextSlot / 9;
        while (invalidSlots.contains(nextSlot % (rows * 9)) || line % 2 == 0) {
            if (i > 1000) throw new RuntimeException("Couldn't find non-invalid slot");
            nextSlot++;
            line = nextSlot / 9;
            i++;
        }
        data.put(nextSlot, slotEntry);
        nextSlot++;
        entries++;
    }

    @Override
    protected String getTitle() {
        return "Player Manager (" + context.getClaim().getName() + ")";
    }

    private void addToggle(UUID uuid, Permission.Value value) {
        Text text;
        ItemStack state;
        switch (value) {
            case TRUE -> {
                state = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
                text = new LiteralText("Trusted").formatted(Formatting.GREEN);
            }
            case FALSE -> {
                state = new ItemStack(Items.RED_STAINED_GLASS_PANE);
                text = new LiteralText("Distrusted").formatted(Formatting.RED);
            }
            case UNSET -> {
                state = new ItemStack(Items.GLASS_PANE);
                text = new LiteralText("Undefined").formatted(Formatting.GRAY);
            }
            default -> throw new IllegalStateException("Unexpected value: " + value);
        }
        ScreenHelper.addLore(state, "Click to cycle");
        state.setCustomName(text);
        SlotEntry<ClaimContext> slotEntry = new SlotEntry<>(state, (claimContext, leftClick, shiftClick) -> {
            int ordinal = value.ordinal() + 1;
            Permission.Value next = Permission.Value.values()[ordinal % 3];
            GameProfile target = ScreenHelper.getProfile(uuid);
            try {
                TrustCommand.execute(player.getCommandSource(), claimContext.getClaim(), target, next);
                draw();
            } catch (CommandSyntaxException e) {
                player.sendMessage(new LiteralText(e.getContext()), false);
            }
        });
        data.put(nextSlot + 8, slotEntry);
    }
}
