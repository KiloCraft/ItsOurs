package me.drex.itsours.claim.permission.util.node.util;


import com.google.common.collect.Lists;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.SharedConstants;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class Node {

    final String id;
    final Permission.Value defaultVal = Permission.Value.UNSET;
    final List<Node> nodes = new ArrayList<>();

    public Node(String id) {
        this.id = id;
    }

    public static <T> List<Node> getNodes(@NotNull final Registry<T> registry, TagGroup<T> tagGroup, List<Node> child, Predicate<T> predicate) {
        Validate.notNull(registry, "Registry must not be null!");
        Validate.notNull(tagGroup, "Identified tag list must not be null!");
        final List<Node> nodes = Lists.newArrayList();
        for (Map.Entry<Identifier, Tag<T>> mapEntry : tagGroup.getTags().entrySet()) {
            Tag<T> tag = mapEntry.getValue();
            final List<Node> entries = Lists.newArrayList();
            for (T entry : registry) {
                if (!predicate.test(entry)) continue;
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
            if (!predicate.test(entry)) continue;
            nodes.add(new Node(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ).addNodes(child));
        }
        return nodes;
    }

    public static <T> List<Node> getNodes(@NotNull final Registry<T> registry, TagGroup<T> tagGroup) {
        return getNodes(registry, tagGroup, Collections.emptyList(), predicate -> true);
    }

    public static <T> List<Node> getNodes(@NotNull final Registry<T> registry, TagGroup<T> tagGroup, Predicate<T> predicate) {
        return getNodes(registry, tagGroup, Collections.emptyList(), predicate);
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

    public List<Node> getNodes() {
        return nodes;
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

    @Override
    public String toString() {
        return "Node[id=" + getId() + ", nodes=" + Arrays.toString(nodes.toArray()) + "]";
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

    public List<Node> getContained() {
        return Collections.singletonList(this);
    }

    public String getId() {
        return id;
    }

    public Permission.Value getDefaultValue() {
        return this.defaultVal;
    }

    public List<Node> getNodes(String input) throws InvalidPermissionException {
        if (input.equals("")) {
            if (SharedConstants.isDevelopment) System.out.println("Input empty");
            return new ArrayList<>();
        } else {
            String id = input.split("\\.")[0];
            if (SharedConstants.isDevelopment) System.out.println("Looking for id \"" + id + "\" input: " + input);
            for (Node node : nodes) {
                if (node.getId().equals(id)) {
                    if (SharedConstants.isDevelopment) System.out.println("node " + node.getId() + " contains " + id);
                    //Properly update input
                    String s = input.contains(".") ? input.substring(input.indexOf('.') + 1) : "";
                    if (SharedConstants.isDevelopment) System.out.println("New id will be " + s);
                    List<Node> list = node.getNodes(s);
                    list.add(node);
                    return list;
                }
            }
            System.out.println("Couldnt find " + id + " in any subnodes");
        }
        throw new InvalidPermissionException("Couldn't find " + id + " node in " + this.getId());
    }
}
