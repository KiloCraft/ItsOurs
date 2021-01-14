package me.drex.itsours.claim.permission.util.newNode.util;


import com.google.common.collect.Lists;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.omg.CORBA.DynAnyPackage.Invalid;

import java.util.*;

public class Node {

    final String id;
    final Permission.Value defaultVal = Permission.Value.UNSET;

    public List<Node> getNodes() {
        return nodes;
    }

    final List<Node> nodes = new ArrayList<>();

    public Node(String id) {
        this.id = id;
    }

    public static <T> List<Node> getNodes(@NotNull final Registry<T> registry, TagGroup<T> tagGroup, List<Node> child, Class<?>... filter) {
        Validate.notNull(registry, "Registry must not be null!");
        Validate.notNull(tagGroup, "Identified tag list must not be null!");
        final List<Node> nodes = Lists.newArrayList();
        for (Map.Entry<Identifier, Tag<T>> mapEntry : tagGroup.getTags().entrySet()) {
            Tag<T> tag = mapEntry.getValue();
            final List<Node> entries = Lists.newArrayList();
            for (T entry : registry) {
                if (filter(entry, filter)) continue;
                if (tag.contains(entry)) {
                    final Identifier identifier = registry.getId(entry);
                    Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.toString());
                    entries.add(new Node(identifier.getPath()));
                }
            }
            if (!entries.isEmpty())
                nodes.add(new GroupNode(mapEntry.getKey().getPath().toUpperCase(Locale.ENGLISH), entries).addNodes(child));
        }
        for (T entry : registry) {
            if (filter(entry, filter)) continue;
            nodes.add(new Node(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ).addNodes(child));
        }
        return nodes;
    }

    public static <T> List<Node> getNodes(@NotNull final Registry<T> registry, TagGroup<T> tagGroup, Class<?>... filter) {
        return getNodes(registry, tagGroup, Collections.emptyList(), filter);
    }

    public static <T> Node of(String id, @NotNull final Registry<T> registry, TagGroup<T> tagGroup, Class<?>... filter) {
        Validate.notNull(registry, "Registry must not be null!");
        Validate.notNull(tagGroup, "Identified tag list must not be null!");
        final List<Node> nodes = Lists.newArrayList();
        for (Map.Entry<Identifier, Tag<T>> mapEntry : tagGroup.getTags().entrySet()) {
            Tag<T> tag = mapEntry.getValue();
            final List<Node> entries = Lists.newArrayList();
            for (T entry : registry) {
                if (filter(entry, filter)) continue;
                if (tag.contains(entry)) {
                    final Identifier identifier = registry.getId(entry);
                    Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.toString());
                    entries.add(new Node(identifier.getPath()));
                }
            }
            if (!entries.isEmpty())
                nodes.add(new GroupNode(mapEntry.getKey().getPath().toUpperCase(Locale.ENGLISH), entries));
        }
        for (T entry : registry) {
            if (filter(entry, filter)) continue;
            nodes.add(new Node(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ));
        }
        return new Node(id).addNodes(nodes);
    }

    private static <T> boolean filter(T entry, Class<?>... filter) {
        if (filter.length > 0) {
            for (Class<?> clazz : filter) {
                if (clazz.isInstance(entry)) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public Node add(Node node) {
        this.nodes.add(node);
        return this;
    }

    public Node add(String node) {
        this.nodes.add(new Node(node));
        return this;
    }

    public Node addNodes(List<Node> node) {
        this.nodes.addAll(node);
        return this;
    }

    public Node addSimpleNodes(List<String> node) {
        for (String s : node) {
            this.add(s);
        }
        return this;
    }

    public boolean contains(String node) {
        return this.getId().equals(node);
    }

    public String getId() {
        return id;
    }

    public Permission.Value getDefaultValue() {
        return this.defaultVal;
    }

    public List<Node> getNodes(String input) throws InvalidPermissionException {
        if (input.equals("")) {
            return new ArrayList<>();
        } else {
            String id = input.split("\\.")[0];
            for (Node node : nodes) {
                if (node.contains(id)) {
                    List<Node> list = node.getNodes(input.substring(id.indexOf('.') + 1));
                    list.add(node);
                    return list;
                }
            }
        }
        throw new InvalidPermissionException("Couldn't find " + id + " node in " + this.getId());
    }
}
