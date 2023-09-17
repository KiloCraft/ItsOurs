package me.drex.itsours.claim.permission.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractChildNode extends AbstractNode implements ChildNode {

    protected final MutableText description;
    protected final ItemConvertible icon;
    private final String id;

    protected AbstractChildNode(String id, MutableText description, List<ChildNode> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate) {
        super(nodes, changePredicate);
        this.id = id;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public ItemConvertible getIcon() {
        return this.icon;
    }

    @Override
    public MutableText getDescription() {
        return this.description;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public String getId() {
        return this.id;
    }

}
