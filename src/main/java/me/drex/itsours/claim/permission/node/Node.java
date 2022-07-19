package me.drex.itsours.claim.permission.node;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.permission.node.builder.RootNodeBuilder;
import me.drex.itsours.claim.permission.node.builder.SingleNodeBuilder;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Node {

    static SingleNodeBuilder single(String id) {
        return new SingleNodeBuilder(id);
    }

    /**
    * Returns a node that may be used for checking permission checking
    * */
    static <T> Node dummy(Registry<T> registry, T value) {
        return new SingleNode(registry.getId(value).getPath(), Text.empty(), Collections.emptyList(), Items.AIR, context -> true);
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

    MutableText getDescription();

    record ChangeContext(@Nullable AbstractClaim claim, WeightedContext context, Value value, ServerCommandSource source) { }

}
