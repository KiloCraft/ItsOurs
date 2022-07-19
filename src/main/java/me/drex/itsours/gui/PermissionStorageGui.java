package me.drex.itsours.gui;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.text.Texts;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PermissionStorageGui extends PagedGui<Node> {

    private final MutableText title;
    private final PermissionHolder permissionHolder;
    private final CommandCallback<Pair<Permission, Value>> commandCallback;
    private final Permission permission;
    private final Node lastNode;

    public PermissionStorageGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, MutableText title, PermissionHolder permissionHolder, CommandCallback<Pair<Permission, Value>> commandCallback, Permission permission) {
        super(player, previousGui);
        this.title = title;
        this.permissionHolder = permissionHolder;
        this.commandCallback = commandCallback;
        this.permission = permission;
        this.lastNode = permission.getLastNode();
        this.setTitle(title.append(Text.translatable("text.itsours.format.separator")).append(permission.asString()));
    }

    @Override
    protected List<Node> getElements() {
        return lastNode.getNodes();
    }

    @Override
    public List<SortMode<Node>> getSortModes() {
        return List.of(
                new SortMode<>(Text.translatable("text.itsours.gui.sortMode.ascending"), Comparator.comparing(Node::getId)),
                new SortMode<>(Text.translatable("text.itsours.gui.sortMode.descending"), Comparator.comparing(Node::getId).reversed())
        );
    }

    @Override
    protected GuiElement asDisplayElement(Node element) {
        Permission withNode = this.permission.withNode(element);
        Value value = permissionHolder.get(withNode);
        var builder = new GuiElementBuilder(element.getIcon().asItem())
                .setName(Text.literal(element.getName()))
                .setLore(List.of(
                        value.format(),
                        element.getDescription().formatted(Formatting.WHITE)
                ))
                .hideFlags()
                .setCallback((index, type, action, gui) -> {
                    if (type.isRight) {
                        if (element.getNodes().isEmpty() && !(element instanceof GroupNode)) {
                            playFailSound(player);
                            return;
                        }
                        SimpleGui simpleGui;
                        if (element instanceof GroupNode groupNode) {
                            simpleGui = new GroupPermissionGui(player, this, title, permissionHolder, commandCallback, withNode, groupNode);
                        } else {
                            simpleGui = new PermissionStorageGui(player, this, title, permissionHolder, commandCallback, withNode);
                        }
                        simpleGui.open();
                    } else {
                        int newIndex = 1;
                        for (Value v : Value.values()) {
                            if (v.equals(value)) break;
                            newIndex++;
                        }
                        Value newValue = Value.values()[newIndex % Value.values().length];
                        try {
                            commandCallback.execute(new Pair<>(withNode, newValue));
                        } catch (CommandSyntaxException exception) {
                            player.getCommandSource().sendError(Texts.toText(exception.getRawMessage()));
                            playFailSound(player);
                        }
                        updateDisplay();
                    }
                });
        if (value.value) builder.glow();
        return builder.build();
    }
}
