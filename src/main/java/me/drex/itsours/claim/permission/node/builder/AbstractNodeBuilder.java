package me.drex.itsours.claim.permission.node.builder;

import me.drex.itsours.claim.permission.node.AbstractNode;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractNodeBuilder {

    protected final String id;
    protected MutableText description = Text.empty();
    protected final List<Node> childNodes = new LinkedList<>();
    protected ItemConvertible icon = Items.STONE;
    protected Predicate<Node.ChangeContext> changePredicate = context -> true;

    public AbstractNodeBuilder(String id) {
        this.id = id;
    }

    public AbstractNodeBuilder description(String translationId) {
        this.description = Text.translatable(translationId);
        return this;
    }

    public AbstractNodeBuilder description(MutableText description) {
        this.description = description;
        return this;
    }

    public AbstractNodeBuilder icon(ItemConvertible icon) {
        this.icon = icon;
        return this;
    }

    public AbstractNodeBuilder then(Collection<Node> childNodes) {
        this.childNodes.addAll(childNodes);
        return this;
    }

    public AbstractNodeBuilder then(Node childNode) {
        this.childNodes.add(childNode);
        return this;
    }

    public AbstractNodeBuilder predicate(Predicate<Node.ChangeContext> changePredicate) {
        this.changePredicate = this.changePredicate.and(changePredicate);
        return this;
    }

    public abstract AbstractNode build();

}
