package me.drex.itsours.claim.flags.node;

import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractChildNode extends AbstractNode implements ChildNode {

    protected final MutableComponent description;
    protected final ItemLike icon;
    private final String id;

    protected AbstractChildNode(String id, MutableComponent description, List<ChildNode> nodes, ItemLike icon, Predicate<ChangeContext> changePredicate) {
        super(nodes, changePredicate);
        this.id = id;
        this.description = description;
        this.icon = icon;
    }

    @Override
    public ItemLike getIcon() {
        return this.icon;
    }

    @Override
    public MutableComponent getDescription() {
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
