package me.drex.itsours.claim.flags.node;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.node.builder.AbstractNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.LiteralNodeBuilder;
import me.drex.itsours.claim.flags.util.Value;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.commands.CommandSourceStack;
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
        return registry(BuiltInRegistries.ENTITY_TYPE, entityType);
    }

    static ChildNode block(Block block) {
        return registry(BuiltInRegistries.BLOCK, block);
    }

    static ChildNode item(Item item) {
        return registry(BuiltInRegistries.ITEM, item);
    }

    static GroupNodeBuilder group(String id) {
        return new GroupNodeBuilder(id);
    }

    List<ChildNode> getNodes();

    Map<String, ChildNode> getNodesMap();

    String getName();


    boolean canChange(ChangeContext context);

    record ChangeContext(@Nullable AbstractClaim claim, WeightedContext context, Value value,
                         CommandSourceStack source) {
    }

}
