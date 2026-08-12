package me.drex.itsours.gui.flags;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.Map;

import static me.drex.itsours.util.LocalizedMessage.splitLines;

public class GroupNodeGui extends PageGui<ChildNode> {

    private final GroupNode groupNode;

    public GroupNodeGui(GuiContext context, GroupNode groupNode, Flag flag) {
        super(context, MenuType.GENERIC_9x6);
        this.groupNode = groupNode;
        setTitle(message("text.itsours.gui.groupnode.title", Map.of("flag", Component.literal(flag.asString()))));
    }

    @Override
    public Collection<ChildNode> elements() {
        return groupNode.getContained();
    }

    @Override
    protected GuiElementBuilder guiElement(ChildNode childNode) {
        return new GuiElementBuilder(childNode.getIcon().asItem())
            .setName(Component.literal(childNode.getName()))
            .setLore(splitLines(childNode.getDescription()));
    }
}
