package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.holder.PermissionHolder;
import me.drex.itsours.claim.permission.node.GroupNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.gui.util.CommandCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GroupPermissionGui extends PermissionStorageGui {

    private final List<Node> contained;

    public GroupPermissionGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, MutableText title, PermissionHolder permissionHolder, CommandCallback<Pair<Permission, Value>> commandCallback, Permission permission, GroupNode groupNode) {
        super(player, previousGui, title, permissionHolder, commandCallback, permission);
        this.contained = new ArrayList<>(groupNode.getContained());
    }

    @Override
    protected List<Node> getElements() {
        return contained;
    }

    @Override
    protected GuiElement asDisplayElement(Node element) {
        var builder = new GuiElementBuilder(element.getIcon().asItem())
                .setName(Text.literal(element.getName()))
                .setLore(List.of(element.getDescription().formatted(Formatting.WHITE)));
        return builder.build();
    }
}
