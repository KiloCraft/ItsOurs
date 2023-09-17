package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.util.Modify;
import me.drex.itsours.claim.roles.ClaimRoleManager;
import me.drex.itsours.command.TrustCommand;
import me.drex.itsours.gui.permission.PersonalStorageGui;
import me.drex.itsours.gui.util.ConfirmationGui;
import me.drex.itsours.gui.util.GuiTextures;
import me.drex.itsours.gui.util.PlayerSelectorGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerManagerGui extends PageGui<UUID> {

    private AbstractClaim claim;
    protected final List<PageGui.Filter<UUID>> filters = List.of(
        new Filter<>(localized("text.itsours.gui.playermanager.filter.all"), player -> true),
        new Filter<>(localized("text.itsours.gui.playermanager.filter.trusted"), player -> claim.getRoleManager().getRole(ClaimRoleManager.TRUSTED).players().contains(player)),
        new Filter<>(localized("text.itsours.gui.playermanager.filter.not_trusted"), player -> !claim.getRoleManager().getRole(ClaimRoleManager.TRUSTED).players().contains(player))
    );

    public PlayerManagerGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.playermanager.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<UUID> elements() {
        Set<UUID> elements = new HashSet<>();
        elements.addAll(claim.getPermissions().keySet());
        claim.getRoleManager().roles().forEach((s, role) -> {
            elements.addAll(role.players());
        });
        return elements.stream().sorted((uuid1, uuid2) ->
                context.server().getUserCache().getByUuid(uuid1).map(GameProfile::getName).orElse(uuid1.toString())
                    .compareTo(context.server().getUserCache().getByUuid(uuid2).map(GameProfile::getName).orElse(uuid2.toString())))
            .toList();
    }

    @Override
    protected GuiElementBuilder guiElement(UUID player) {
        return guiElement(Items.PLAYER_HEAD, "playermanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of("roles", PlaceholderUtil.list(
                claim.getRoleManager().roles().entrySet().stream().filter(stringRoleEntry -> stringRoleEntry.getValue().players().contains(player)).toList(),
                stringRoleEntry -> Map.of(
                    "role_id", Text.literal(stringRoleEntry.getKey())
                ), "text.itsours.gui.playermanager.entry.roles"
            )/*, "modified_permissions", Text.literal(String.valueOf(claim.getPermissions().getOrDefault(player, new PermissionHolder()).size()))*/),
            claim.getPermissions().getOrDefault(player, new PermissionData()).placeholders(),
            PlaceholderUtil.uuid("player_", player, context.server())
        )).setCallback(clickType -> {
            if (clickType.isLeft) {
                switchUi(new PersonalStorageGui(context, claim, player, Permission.permission(PermissionManager.PERMISSION)));
            } else if (clickType.isRight) {
                switchUi(new PlayerRoleManagerGui(context, claim, player));
            } else if (clickType.isMiddle) {
                switchUi(new ConfirmationGui(context, "text.itsours.gui.playermanager.reset.confirm", PlaceholderUtil.uuid("player_", player, context.server()), () -> {
                    // Reset player
                    if (claim.hasPermission(context.player.getUuid(), PermissionManager.MODIFY, Modify.PERMISSION.node())) {
                        claim.getPermissions().remove(player);
                        claim.getRoleManager().roles().forEach((s, role) -> role.players().remove(player));
                        build();
                    } else {
                        fail();
                    }
                }));
            }
        });
    }

    @Override
    public CompletableFuture<GuiElementBuilder> guiElementFuture(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> guiElement(uuid)
            .setSkullOwner(new GameProfile(uuid, null), context.server()), Util.getMainWorkerExecutor());
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        return switch (index) {
            case 0 -> new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.playermanager.add"))
                .hideFlags()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new PlayerSelectorGui(context, this::addPlayer));
                });
            case 1 -> new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.playermanager.trust"))
                .hideFlags()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new PlayerSelectorGui(context, this::trustPlayer));
                });
            default -> super.buildNavigationBar(index);
        };
    }

    @Override
    protected List<Filter<UUID>> filters() {
        return this.filters;
    }

    private void addPlayer(String name) {
        CompletableFuture.runAsync(() -> {
            context.server().getUserCache().findByName(name).ifPresentOrElse(gameProfile -> {
                claim.getPermissions().putIfAbsent(gameProfile.getId(), new PermissionData());
                build();
            }, this::fail);
        }, context.server());
    }

    private void trustPlayer(String name) {
        CompletableFuture.runAsync(() -> {
            context.server().getUserCache().findByName(name).ifPresentOrElse(gameProfile -> {
                try {
                    TrustCommand.TRUST.executeTrust(context.player.getCommandSource(), claim, Collections.singleton(gameProfile));
                    build();
                } catch (CommandSyntaxException ignored) {
                    fail();
                }
            }, this::fail);
        }, context.server());
    }

}
