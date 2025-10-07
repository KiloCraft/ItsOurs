package me.drex.itsours.gui.players;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.command.TrustCommand;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import me.drex.itsours.gui.flags.PlayerFlagsGui;
import me.drex.itsours.gui.players.PlayerGroupManagerGui;
import me.drex.itsours.gui.util.ConfirmationGui;
import me.drex.itsours.gui.util.GuiTextures;
import me.drex.itsours.gui.util.PlayerSelectorGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.drex.message.api.LocalizedMessage.localized;

public class AdvancedPlayerManagerGui extends PageGui<UUID> {

    private AbstractClaim claim;
    protected final List<PageGui.Filter<UUID>> filters = List.of(
        new Filter<>(localized("text.itsours.gui.playermanager.filter.all"), player -> true),
        new Filter<>(localized("text.itsours.gui.playermanager.filter.trusted"), player -> claim.getGroupManager().trusted.players().contains(player)),
        new Filter<>(localized("text.itsours.gui.playermanager.filter.not_trusted"), player -> !claim.getGroupManager().trusted.players().contains(player))
    );

    public AdvancedPlayerManagerGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.playermanager.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<UUID> elements() {
        Set<UUID> elements = new HashSet<>();
        elements.addAll(claim.getPlayerFlags().keySet());
        claim.getGroupManager().groups().forEach((s, group) -> {
            elements.addAll(group.players());
        });
        return elements.stream().sorted((uuid1, uuid2) ->
                context.server().getApiServices().nameToIdCache().getByUuid(uuid1).map(PlayerConfigEntry::name).orElse(uuid1.toString())
                    .compareTo(context.server().getApiServices().nameToIdCache().getByUuid(uuid2).map(PlayerConfigEntry::name).orElse(uuid2.toString())))
            .toList();
    }

    @Override
    protected GuiElementBuilder guiElement(UUID player) {
        FlagData playerFlags = claim.getPlayerFlags().getOrDefault(player, new FlagData());
        return guiElement(Items.PLAYER_HEAD, "playermanager.entry", PlaceholderUtil.mergePlaceholderMaps(
            claim.placeholders(context.server()),
            Map.of(
                "groups", PlaceholderUtil.list(claim.getGroupManager().groups().entrySet().stream().filter(entry -> entry.getValue().players().contains(player)).toList(),
                    groupEntry -> Map.of(
                        "group_id", Text.literal(groupEntry.getKey())
                    ), "text.itsours.gui.playermanager.entry.groups"),
                "flags", playerFlags.toText()),
            PlaceholderUtil.uuid("player_", player, context.server())
        )).setCallback(clickType -> {
            if (clickType.isLeft) {
                switchUi(new PlayerFlagsGui(context, claim, player, Flag.flag(Flags.PLAYER)));
            } else if (clickType.isRight) {
                switchUi(new PlayerGroupManagerGui(context, claim, player));
            } else if (clickType.isMiddle) {
                switchUi(new ConfirmationGui(context, "text.itsours.gui.playermanager.reset.confirm", PlaceholderUtil.uuid("player_", player, context.server()), () -> {
                    // Reset player
                    if (claim.checkAction(context.player.getUuid(), Flags.MODIFY, Modify.FLAG.node())) {
                        claim.getPlayerFlags().remove(player);
                        claim.getGroupManager().groups().forEach((s, group) -> group.players().remove(player));
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
            .setSkullOwner(new GameProfile(uuid, uuid.toString()), context.server()), Util.getMainWorkerExecutor());
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        return switch (index) {
            case 0 -> new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.playermanager.add"))
                .hideDefaultTooltip()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new PlayerSelectorGui(context, this::addPlayer));
                });
            case 1 -> new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.playermanager.trust"))
                .hideDefaultTooltip()
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
            context.server().getApiServices().profileRepository().findProfileByName(name).ifPresentOrElse(gameProfile -> {
                claim.getPlayerFlags().putIfAbsent(gameProfile.id(), new FlagData());
                build();
            }, this::fail);
        }, context.server());
    }

    private void trustPlayer(String name) {
        CompletableFuture.runAsync(() -> {
            context.server().getApiServices().profileRepository().findProfileByName(name).ifPresentOrElse(gameProfile -> {
                try {
                    TrustCommand.TRUST.executeTrust(context.player.getCommandSource(), claim, Collections.singleton(new PlayerConfigEntry(gameProfile)));
                    build();
                } catch (CommandSyntaxException ignored) {
                    fail();
                }
            }, this::fail);
        }, context.server());
    }

}
