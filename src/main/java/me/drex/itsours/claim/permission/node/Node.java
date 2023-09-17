package me.drex.itsours.claim.permission.node;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.permission.node.builder.LiteralNodeBuilder;
import me.drex.itsours.claim.permission.util.Value;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface Node {

    static LiteralNodeBuilder literal(String literal) {
        return new LiteralNodeBuilder(literal);
    }

    /**
     * Returns a node that may be used for checking permission checking
     */
    static <T> ChildNode registry(Registry<T> registry, T value) {
        return new LiteralNode(registry.getId(value).getPath(), Text.empty(), Collections.emptyList(), Items.AIR, context -> true);
    }

    static GroupNodeBuilder group(String id) {
        return new GroupNodeBuilder(id);
    }

    List<ChildNode> getNodes();

    Map<String, ChildNode> getNodesMap();

    String getName();


    boolean canChange(ChangeContext context);

    record ChangeContext(@Nullable AbstractClaim claim, WeightedContext context, Value value,
                         ServerCommandSource source) {
    }

}
