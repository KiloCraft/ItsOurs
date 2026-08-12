package me.drex.itsours.gui.flags;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import me.drex.itsours.claim.flags.node.LiteralNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.InvalidFlagException;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.itsours.util.LocalizedMessage.splitLines;

public abstract class AbstractFlagsGui extends PageGui<ChildNode> {

    protected FlagData flagData;
    protected Flag flag;
    protected final List<PageGui.Filter<ChildNode>> filters = List.of(
        new Filter<>(message("text.itsours.gui.flags.advanced.filter.all"), childNode -> true),
        new Filter<>(message("text.itsours.gui.flags.advanced.filter.single"), childNode -> childNode instanceof LiteralNode),
        new Filter<>(message("text.itsours.gui.flags.advanced.filter.group"), childNode -> childNode instanceof GroupNode),
        new Filter<>(message("text.itsours.gui.flags.advanced.filter.modified"), childNode -> {
            try {
                return flagData.get(flag.withNode(childNode)) != Value.DEFAULT;
            } catch (InvalidFlagException ignored) {
                return true;
            }
        })
    );

    protected AbstractFlagsGui(GuiContext context, FlagData flagData, Flag flag) {
        super(context, MenuType.GENERIC_9x6);
        this.flagData = flagData;
        this.flag = flag;
    }

    @Override
    public Collection<ChildNode> elements() {
        return flag.getLastNode().getNodes().stream().filter(elementFilter()).toList();
    }

    public Predicate<ChildNode> elementFilter() {
        return (childNode) -> true;
    }

    @Override
    protected GuiElementBuilder guiElement(ChildNode childNode) {
        try {
            Flag withNode = flag.withNode(childNode);
            Node lastNode = withNode.getLastNode();
            Value value = flagData.get(withNode);
            boolean isSimpleNode = lastNode.getNodes().isEmpty();
            String localizationId;
            if (lastNode instanceof GroupNode) {
                localizationId = "text.itsours.gui.flags.advanced.element.lore.group";
            } else {
                if (isSimpleNode) {
                    localizationId = "text.itsours.gui.flags.advanced.element.lore.simple";
                } else {
                    localizationId = "text.itsours.gui.flags.advanced.element.lore.complex";
                }
            }
            int damage = switch (value) {
                case ALLOW -> 1;
                case DEFAULT -> 0;
                case DENY -> Integer.MAX_VALUE;
            };
            return new GuiElementBuilder(childNode.getIcon().asItem())
                .setName(Component.literal(childNode.getName()))
                .setLore(splitLines(message(localizationId, Map.of("description", childNode.getDescription(), "value", value.format()))))
                .hideDefaultTooltip()
                .setComponent(DataComponents.MAX_STACK_SIZE, 1)
                .setComponent(DataComponents.MAX_DAMAGE, Integer.MAX_VALUE)
                .setComponent(DataComponents.DAMAGE, damage)
                .setCallback(clickType -> {
                    if (clickType.isLeft) {
                        Value nextValue = value.next();
                        if (setValue(withNode, nextValue)) {
                            click();
                            build();
                        } else {
                            fail();
                        }
                    } else if (clickType.isRight) {
                        if (!isSimpleNode) {
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
        } catch (InvalidFlagException ignored) {
            return new GuiElementBuilder();
        }
    }

    @Override
    protected List<Filter<ChildNode>> filters() {
        return filters;
    }

    abstract boolean setValue(Flag flag, Value value);

    abstract AbstractFlagsGui create(Flag flag);

}
