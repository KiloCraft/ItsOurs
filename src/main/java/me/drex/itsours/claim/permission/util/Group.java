package me.drex.itsours.claim.permission.util;

import com.google.common.collect.Lists;
import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.claim.permission.util.node.GroupNode;
import me.drex.itsours.claim.permission.util.node.SingleNode;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class Group {
    public final List<AbstractNode> list;

    public Group(final List<AbstractNode> list) {
        this.list = list;
    }

    public static final Group BLOCK = create(Registry.BLOCK, BlockTags.getRequiredTags());

    public static final Group ENTITIES = create(Registry.ENTITY_TYPE, EntityTypeTags.getRequiredTags());

    public static <T> Group create(@NotNull final Registry<T> registry, @NotNull final List<? extends Tag.Identified<T>> list) {
        Validate.notNull(registry, "Registry must not be null!");
        Validate.notNull(list, "Identified tag list must not be null!");
        final List<AbstractNode> nodes = Lists.newArrayList();

        for (final Tag.Identified<T> tag : list) {
            final List<String> entries = Lists.newArrayList();

            for (T entry : registry) {
                if (tag.contains(entry)) {
                    final Identifier id = registry.getId(entry);
                    Validate.notNull(id, "%s does not contain entry %s", registry.toString(), entry.toString());
                    entries.add(id.getPath());
                }
            }

            nodes.add(new GroupNode(tag.getId().getPath().toUpperCase(Locale.ENGLISH), entries.toArray(new String[0])));
        }

        for (T entry : registry) {
            nodes.add(new SingleNode(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ));
        }

        return new Group(nodes);
    }
}
