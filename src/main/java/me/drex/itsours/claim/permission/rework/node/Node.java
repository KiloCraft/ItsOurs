package me.drex.itsours.claim.permission.rework.node;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.rework.Value;
import me.drex.itsours.claim.permission.rework.context.WeightedContext;
import me.drex.itsours.claim.permission.rework.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.permission.rework.node.builder.RootNodeBuilder;
import me.drex.itsours.claim.permission.rework.node.builder.SingleNodeBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface Node {

    static SingleNodeBuilder single(String id) {
        return new SingleNodeBuilder(id);
    }

    static GroupNodeBuilder group(String id) {
        return new GroupNodeBuilder(id);
    }

    static RootNodeBuilder root(String name) {
        return new RootNodeBuilder(name);
    }

    List<Node> getNodes();

    Map<String, Node> getNodesMap();

    String getId();

    String getName();

    boolean contains(Node other);

    boolean canChange(ChangeContext context);

    ItemConvertible getIcon();

    Text getDescription();

    record ChangeContext(AbstractClaim claim, WeightedContext context, Value value, ServerCommandSource source) { }

}
