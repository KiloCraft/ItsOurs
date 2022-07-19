package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionImpl;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.claim.permission.holder.ClaimPermissionHolder;
import me.drex.itsours.claim.permission.roles.Role;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.command.PermissionsCommand;
import me.drex.itsours.command.TrustCommand;
import me.drex.itsours.util.Components;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerManagerGui extends PagedGui<UUID> {

    private final AbstractClaim claim;
    private static final Role TRUSTED_ROLE = RoleManager.INSTANCE.getRole(RoleManager.TRUSTED_ID);

    public PlayerManagerGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, AbstractClaim claim) {
        super(player, previousGui);
        this.claim = claim;
        this.setTitle(Text.translatable("text.itsours.gui.roles"));
    }

    @Override
    protected List<UUID> getElements() {
        ClaimPermissionHolder permissionHolder = claim.getPermissionHolder();
        Set<UUID> uuids = new HashSet<>(permissionHolder.getRoles().keySet());
        uuids.addAll(permissionHolder.getPlayerPermissions().keySet());
        return uuids.stream().toList();
    }

    @Override
    protected GuiElement getNavElement(int id) {
        return switch (id) {
            case 0 -> new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setName(Text.translatable("text.itsours.gui.roles.addTrust"))
                    .hideFlags()
                    .setSkullOwner(GuiTextures.GUI_ADD)
                    .setCallback((x, y, z) -> {
                        new PlayerSelectorGui(player, this, name -> TrustCommand.TRUST.executeTrust(player.getCommandSource().withSilent(), claim, asCommandTarget(name))).open();
                        playClickSound(this.player);
                    }).build();
            default -> super.getNavElement(id);
        };
    }

    @Override
    protected GuiElement asDisplayElement(UUID uuid) {
        Optional<GameProfile> optional = player.server.getUserCache().getByUuid(uuid);
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder(Items.PLAYER_HEAD);
        if (optional.isPresent()) {
            GameProfile profile = optional.get();
            if (profile.getName() != null) {
                guiElementBuilder
                        .setSkullOwner(profile, null);
            }
        }
        boolean trusted = claim.getPermissionHolder().getRoles(uuid).contains(TRUSTED_ROLE);
        MutableText name = Components.toText(uuid).formatted(Formatting.WHITE);
        if (trusted)
            name.append(" ").append(Text.translatable("chat.square_brackets", Text.literal(RoleManager.TRUSTED_ID).formatted(Formatting.GREEN)));
        return
                guiElementBuilder
                        .setLore(
                                List.of(
                                        Text.translatable("text.itsours.gui.leftClick",
                                                trusted ? Text.translatable("text.itsours.gui.roles.distrust") : Text.translatable("text.itsours.gui.roles.trust")
                                        ).formatted(Formatting.WHITE),
                                        Text.translatable("text.itsours.gui.rightClick", Text.translatable("text.itsours.gui.roles.permission")).formatted(Formatting.WHITE)
                                )
                        )
                        .setName(name)
                        .setCallback((index, type, action, gui) -> {
                            GameProfile profile = player.server.getUserCache().getByUuid(uuid).orElse(new GameProfile(uuid, null));
                            Set<GameProfile> targets = Collections.singleton(profile);
                            if (type.isLeft) {
                                try {
                                    if (trusted) {
                                        TrustCommand.DISTRUST.executeTrust(player.getCommandSource().withSilent(), claim, targets);
                                    } else {
                                        TrustCommand.TRUST.executeTrust(player.getCommandSource().withSilent(), claim, targets);
                                    }
                                } catch (CommandSyntaxException exception) {
                                    player.getCommandSource().sendError(Texts.toText(exception.getRawMessage()));
                                }
                                updateDisplay();
                            } else {
                                new PermissionStorageGui(player, this, Text.translatable("text.itsours.gui.permission"), claim.getPermissionHolder().getPermission(uuid),
                                        (pair) -> PermissionsCommand.INSTANCE.executeSet(player.getCommandSource().withSilent(), claim, targets, pair.getLeft(), pair.getRight()),
                                        PermissionImpl.withNodes(PermissionManager.PERMISSION)).open();
                            }
                        })
                        .hideFlags()
                        .build();
    }
}
