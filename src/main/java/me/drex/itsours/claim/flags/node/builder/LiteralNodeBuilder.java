package me.drex.itsours.claim.flags.node.builder;

import me.drex.itsours.claim.flags.node.LiteralNode;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;

public class LiteralNodeBuilder extends AbstractNodeBuilder {

    public LiteralNodeBuilder(String id) {
        super(id);
    }

    public <T> LiteralNodeBuilder(Registry<T> registry, T value) {
        super(toShortIdentifier(registry.getId(value)));
        ItemConvertible item = getItem(value);
        if (item != null) {
            icon(item);
        }
    }

    @Override
    public LiteralNode build() {
        sortChildNodes();
        return new LiteralNode(id, description, childNodes, icon, changePredicate);
    }
}
