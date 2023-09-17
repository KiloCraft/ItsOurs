package me.drex.itsours.gui.permission;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.holder.PermissionData;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.GroupNode;
import me.drex.itsours.claim.permission.node.LiteralNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.util.InvalidPermissionException;
import me.drex.itsours.claim.permission.util.Value;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public abstract class PermissionStorageGui extends PageGui<ChildNode> {

    protected PermissionData permissionData;
    protected Permission permission;
    protected final List<PageGui.Filter<ChildNode>> filters = List.of(
        new Filter<>(localized("text.itsours.gui.permissionstorage.filter.all"), childNode -> true),
        new Filter<>(localized("text.itsours.gui.permissionstorage.filter.single"), childNode -> childNode instanceof LiteralNode),
        new Filter<>(localized("text.itsours.gui.permissionstorage.filter.group"), childNode -> childNode instanceof GroupNode),
        new Filter<>(localized("text.itsours.gui.permissionstorage.filter.modified"), childNode -> {
            try {
                return permissionData.get(permission.withNode(childNode)) != Value.UNSET;
            } catch (InvalidPermissionException ignored) {
                return true;
            }
        })
    );

    protected PermissionStorageGui(GuiContext context, PermissionData permissionData, Permission permission) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.permissionData = permissionData;
        this.permission = permission;
    }

    @Override
    public Collection<ChildNode> elements() {
        return permission.getLastNode().getNodes().stream().filter(elementFilter()).toList();
    }

    public Predicate<ChildNode> elementFilter() {
        return (childNode) -> true;
    }

    @Override
    protected GuiElementBuilder guiElement(ChildNode childNode) {
        try {
            Permission withNode = permission.withNode(childNode);
            Node lastNode = withNode.getLastNode();
            Value value = permissionData.get(withNode);
            GuiElementBuilder builder = new GuiElementBuilder(childNode.getIcon().asItem())
                .setName(Text.literal(childNode.getName()))
                .addLoreLine(localized("text.itsours.gui.permissionstorage.element.lore", Map.of("description", childNode.getDescription(), "value", value.format())))
                .hideFlags()
                .setCallback(clickType -> {
                    if (clickType.isLeft) {
                        Value nextValue = nextValue(value, withNode);
                        if (setValue(withNode, nextValue)) {
                            click();
                            build();
                        } else {
                            fail();
                        }
                    } else if (clickType.isRight) {
                        if (!lastNode.getNodes().isEmpty()) {
                            switchUi(create(withNode));
                        } else {
                            fail();
                        }
                    } else if (clickType.isMiddle) {
                        if (lastNode instanceof GroupNode groupNode) {
                            switchUi(new GroupNodeGui(context, groupNode, withNode));
                        } else {
                            fail();
                        }
                    }
                });
            if (value.value) builder.glow();
            return builder;
        } catch (InvalidPermissionException ignored) {
            return new GuiElementBuilder();
        }
    }

    @Override
    protected List<Filter<ChildNode>> filters() {
        return filters;
    }

    abstract Value getResult(Permission permission);

    abstract boolean setValue(Permission permission, Value value);

    abstract PermissionStorageGui create(Permission permission);

    Value nextValue(Value value, Permission permission) {
        Value result = getResult(permission);
        if (result == Value.ALLOW) {
            if (value == Value.DENY) {
                return Value.UNSET;
            } else {
                return Value.DENY;
            }
        } else {
            if (value == Value.ALLOW) {
                return Value.UNSET;
            } else {
                return Value.ALLOW;
            }
        }
    }
}
