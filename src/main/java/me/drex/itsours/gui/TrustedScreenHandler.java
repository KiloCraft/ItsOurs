package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.command.TrustCommand;
import me.drex.itsours.command.TrustedCommand;
import me.drex.itsours.util.Pair;
import me.drex.itsours.util.TextComponentUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TrustedScreenHandler extends PagedScreenHandler {

    protected final AbstractClaim claim;
    int maxPage = 0;
    private List<Pair<UUID, Permission.Value>> cache = new ArrayList<>();

    protected TrustedScreenHandler(int syncId, PlayerEntity player, AbstractClaim claim, GUIScreenHandler previous, int page) {
        super(syncId, 6, player);
        this.claim = claim;
        this.previous = previous;
        this.page = page;
        fillInventory(player, this.inventory);
    }

    public static void openMenu(ServerPlayerEntity player, AbstractClaim claim, GUIScreenHandler previous, int page) {
        NamedScreenHandlerFactory factory = new NamedScreenHandlerFactory() {
            @Override
            public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                return new TrustedScreenHandler(syncId, player, claim, previous, page);
            }

            @Override
            public Text getDisplayName() {
                //TODO:
                return new LiteralText("Trustmanager");
            }
        };
        player.openHandledScreen(factory);
    }

    @Override
    protected void fillInventory(PlayerEntity player, Inventory inv) {
        inv.clear();
        cache.clear();
        ItemStack untrustEveryone = new ItemStack(Items.REDSTONE_BLOCK);
        untrustEveryone.setCustomName(new LiteralText("Untrust everyone").formatted(Formatting.DARK_RED, Formatting.BOLD));
        //inventory.setStack(8, untrustEveryone);

        addBack();

        List<UUID> allUUIDs = TrustedCommand.getAllUUIDs(claim).stream().toList();
        //TODO: Add nearby players
        int entriesPerPage = 14;
        maxPage = (allUUIDs.size() - 1) / entriesPerPage;
        super.fillInventory(player, inv);
        fillEmpty();
        CompletableFuture.runAsync(() -> {
            for (int i = 0; i < entriesPerPage; i++) {
                int listIndex = (page * entriesPerPage) + i;
                if (listIndex >= allUUIDs.size()) break;
                int slotIndex;
                if (i < 7) {
                    slotIndex = i + 10;
                } else {
                    slotIndex = i + 21;
                }
                //10-16
                //28-34
                UUID uuid = allUUIDs.get(listIndex);
                ItemStack itemStack = ScreenHelper.createPlayerHead(uuid);
                inventory.setStack(slotIndex, itemStack);
                Role trusted = ItsOursMod.INSTANCE.getRoleManager().getRole("trusted");
                Text text;
                ItemStack state;
                if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRemoved().contains(trusted)) {
                    state = new ItemStack(Items.RED_STAINED_GLASS_PANE);
                    ScreenHelper.addLore(state, TextComponentUtil.of("<green>Trust<reset> <white>/ <red><underlined>Distrust<reset> <white>/ <yellow>Unset"));
                    text = new LiteralText("Distrusted").formatted(Formatting.RED);
                    cache.add(new Pair<>(uuid, Permission.Value.FALSE));
                } else if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRoles().containsKey(trusted)) {
                    state = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
                    ScreenHelper.addLore(state, TextComponentUtil.of("<green><underlined>Trust<reset> <white>/ <red>Distrust<reset> <white>/ <yellow>Unset"));
                    text = new LiteralText("Trusted").formatted(Formatting.GREEN);
                    cache.add(new Pair<>(uuid, Permission.Value.TRUE));
                } else {
                    state = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
                    ScreenHelper.addLore(state, TextComponentUtil.of("<green>Trust<reset> <white>/ <red>Distrust<reset> <white>/ <yellow><underlined>Unset"));
                    text = new LiteralText("Unset").formatted(Formatting.YELLOW);
                    cache.add(new Pair<>(uuid, Permission.Value.UNSET));
                }
                ScreenHelper.addLore(state, TextComponentUtil.of("<white>Right-/Leftclick to cycle"));
                state.setCustomName(text);
                inventory.setStack(slotIndex + 9, state);
                /*ScreenHelper.insertPlayerHeadAsync(player.getServer(), uuid, itemStack -> {
                    inventory.setStack(slotIndex, itemStack);
                    Role trusted = ItsOursMod.INSTANCE.getRoleManager().getRole("trusted");
                    Text text;
                    ItemStack state;
                    if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRemoved().contains(trusted)) {
                        state = new ItemStack(Items.RED_STAINED_GLASS_PANE);
                        ScreenHelper.addLore(state, TextComponentUtil.of("<green>Trust<reset> <white>/ <red><underlined>Distrust<reset> <white>/ <yellow>Unset"));
                        text = new LiteralText("Distrusted").formatted(Formatting.RED);
                        cache.add(new Pair<>(uuid, Permission.Value.FALSE));
                    } else if (claim.getPermissionManager().getPlayerRoleManager(uuid).getRoles().containsKey(trusted)) {
                        state = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
                        ScreenHelper.addLore(state, TextComponentUtil.of("<green><underlined>Trust<reset> <white>/ <red>Distrust<reset> <white>/ <yellow>Unset"));
                        text = new LiteralText("Trusted").formatted(Formatting.GREEN);
                        cache.add(new Pair<>(uuid, Permission.Value.TRUE));
                    } else {
                        state = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
                        ScreenHelper.addLore(state, TextComponentUtil.of("<green>Trust<reset> <white>/ <red>Distrust<reset> <white>/ <yellow><underlined>Unset"));
                        text = new LiteralText("Unset").formatted(Formatting.YELLOW);
                        cache.add(new Pair<>(uuid, Permission.Value.UNSET));
                    }
                    ScreenHelper.addLore(state, TextComponentUtil.of("<white>Right-/Leftclick to cycle"));
                    state.setCustomName(text);
                    inventory.setStack(slotIndex + 9, state);
                });*/
            }
        });


    }

    @Override
    public int getMaxPage() {
        return maxPage;
    }

    @Override
    protected void handleSlotClick(ServerPlayerEntity player, int index, Slot slot, boolean leftClick, boolean shift) {
        super.handleSlotClick(player, index, slot, leftClick, shift);
        switch (index) {
            case 0 -> {
                ScreenHelper.openPrevious(player, this);
            }
            default -> {
                int glassPaneIndex = -1;
                if (index > 18 && index < 26) {
                    glassPaneIndex = index - 19;
                } else if (index > 36 && index < 44) {
                    glassPaneIndex = index - 30;
                }
                if (glassPaneIndex != -1) {
                    if (glassPaneIndex >= cache.size()) return;
                    Pair<UUID, Permission.Value> pair = cache.get(glassPaneIndex);
                    int ordinal = pair.getValue().ordinal() + (leftClick ? 1 : 2);
                    Permission.Value next = Permission.Value.values()[ordinal % 3];
                    GameProfile target = ItsOursMod.server.getUserCache().getByUuid(pair.getKey());
                    try {
                        TrustCommand.execute(player.getCommandSource(), claim, target, next);
                        fillInventory(player, inventory);
                    } catch (CommandSyntaxException e) {
                        player.sendMessage(new LiteralText(e.getContext()), false);
                    }
                    break;
                }
                int headIndex = -1;
                if (index > 9 && index < 17) {
                    headIndex = index - 10;
                } else if (index > 27 && index < 35) {
                    headIndex = index - 21;
                }
                if (headIndex != -1) {
                    if (headIndex >= cache.size()) return;
                    Pair<UUID, Permission.Value> pair = cache.get(headIndex);
                    PermissionInfoScreenHandler.openMenu(player, claim.getPermissionManager().getPlayerPermission(pair.getKey()), this, 0, pair.getKey(), PermissionList.permission);
                }
            }

        }
    }
}
