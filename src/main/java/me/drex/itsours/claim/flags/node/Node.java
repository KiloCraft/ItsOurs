package me.drex.itsours.claim.flags.node;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.node.builder.AbstractNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.LiteralNodeBuilder;
import me.drex.itsours.claim.flags.util.Value;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Node {

    static LiteralNodeBuilder literal(String literal) {
        return new LiteralNodeBuilder(literal);
    }

    static <T> GroupNode group(Registry<T> registry, TagKey<T> tagKey) {
        return new GroupNodeBuilder(registry, tagKey, t -> true).build();
    }


    static <T> ChildNode registry(Registry<T> registry, T value) {
        return new LiteralNodeBuilder(registry, value).build();
    }

    static ChildNode entity(EntityType<?> entityType) {
        return registry(Registries.ENTITY_TYPE, entityType);
    }

    static ChildNode block(Block block) {
        return registry(Registries.BLOCK, block);
    }

    static ChildNode item(Item item) {
        return registry(Registries.ITEM, item);
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
