package me.drex.itsours.gui.players;

import com.mojang.authlib.GameProfile;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.claim.groups.ClaimGroupManager;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import me.drex.itsours.gui.util.GuiTextures;
import me.drex.itsours.gui.util.PlayerSelectorGui;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Util;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.drex.message.api.LocalizedMessage.localized;

public class SimplePlayerManagerGui extends PageGui<UUID> {

    private final AbstractClaim claim;

    public SimplePlayerManagerGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.playermanager.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<UUID> elements() {
        Set<UUID> players = new TreeSet<>();
        players.addAll(claim.getGroupManager().moderator.players());
        players.addAll(claim.getGroupManager().trusted.players());
        return players;
    }

    @Override
    protected GuiElementBuilder guiElement(UUID uuid) {
        ClaimGroupManager groupManager = claim.getGroupManager();
        boolean moderator = groupManager.moderator.players().contains(uuid);
        if (moderator) {
            return guiElement(Items.PLAYER_HEAD, "playermanager.simple.entry.moderator", PlaceholderUtil.uuid("player_", uuid, context.server()))
                .setCallback(clickType -> {
                    if (!claim.checkAction(player.getUuid(), Flags.MODIFY, Modify.FLAG.node())) {
                        fail();
                        return;
                    }
                    if (clickType.isRight) {
                        groupManager.moderator.players().remove(uuid);
                        click();
                        build();
                    } else {
                        fail();
                    }
                });
        } else {
            return guiElement(Items.PLAYER_HEAD, "playermanager.simple.entry.trusted", PlaceholderUtil.uuid("player_", uuid, context.server()))
                .setCallback(clickType -> {
                    if (!claim.checkAction(player.getUuid(), Flags.MODIFY, Modify.FLAG.node())) {
                        fail();
                        return;
                    }
                    if (clickType.isLeft) {
                        groupManager.moderator.players().add(uuid);
                        click();
                        build();
                    } else if (clickType.isRight) {
                        groupManager.trusted.players().remove(uuid);
                        click();
                        build();
                    } else {
                        fail();
                    }
                });
        }
    }

    @Override
    public CompletableFuture<GuiElementBuilder> guiElementFuture(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> guiElement(uuid)
            .setSkullOwner(new GameProfile(uuid, uuid.toString()), context.server()), Util.getMainWorkerExecutor());
    }

    @Override
    public GuiElementBuilder buildNavigationBar(int index) {
        if (index == 0) {
            return new GuiElementBuilder(Items.PLAYER_HEAD)
                .setName(localized("text.itsours.gui.playermanager.simple.trust"))
                .hideDefaultTooltip()
                .setSkullOwner(GuiTextures.GUI_ADD)
                .setCallback(() -> {
                    switchUi(new PlayerSelectorGui(context, playerName -> {
                        context.server().getUserCache().findByNameAsync(playerName).thenAcceptAsync(optionalGameProfile -> {
                            optionalGameProfile.ifPresentOrElse(gameProfile -> {
                                claim.getGroupManager().trusted.players().add(gameProfile.getId());
                                click();
                                build();
                            }, this::fail);
                        }, context.server());
                    }));
                });
        };
        return super.buildNavigationBar(index);
    }

}
