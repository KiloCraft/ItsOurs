package me.drex.itsours.gui.flags;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class GroupNodeGui extends PageGui<ChildNode> {

    private final GroupNode groupNode;

    public GroupNodeGui(GuiContext context, GroupNode groupNode, Flag flag) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.groupNode = groupNode;
        setTitle(localized("text.itsours.gui.groupnode.title", Map.of("flag", Text.literal(flag.asString()))));
    }

    @Override
    public Collection<ChildNode> elements() {
        return groupNode.getContained();
    }

    @Override
    protected GuiElementBuilder guiElement(ChildNode childNode) {
        return new GuiElementBuilder(childNode.getIcon().asItem())
            .setName(Text.literal(childNode.getName()))
            .setLore(List.of(childNode.getDescription()));
    }
}
