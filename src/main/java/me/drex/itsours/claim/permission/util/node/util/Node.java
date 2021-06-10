package me.drex.itsours.claim.permission.util.node.util;


import com.google.common.collect.Lists;
import me.drex.itsours.claim.permission.Permission;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public class Node {

    protected final String id;
    final Permission.Value defaultVal = Permission.Value.UNSET;
    final List<Node> nodes = new ArrayList<>();
    Item item = Items.STONE;
    String information = "-";

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
            T symbol = null;
            for (T entry : registry) {
                if (!predicate.test(entry)) continue;
                if (tag.contains(entry)) {
                    if (symbol == null) symbol = entry;
                    final Identifier identifier = registry.getId(entry);
                    Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.toString());
                    Node n = new Node(identifier.getPath());
                    addItem(entry, n);
                    entries.add(n);
                }
            }
            if (!entries.isEmpty()) {
                GroupNode groupNode = new GroupNode(mapEntry.getKey().getPath().toUpperCase(Locale.ENGLISH), entries);
                addItem(symbol, groupNode);
                nodes.add(groupNode.addNodes(child));
            }
        }
        for (T entry : registry) {
            Node n = new Node(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ).addNodes(child);
            addItem(entry, n);
            if (!predicate.test(entry)) continue;
            nodes.add(n);
        }
        return nodes;
    }

    private static <T>void addItem(T entry, Node n) {
        if (entry instanceof ItemConvertible) {
            n.item(((ItemConvertible) entry).asItem());
        } else if (entry instanceof EntityType) {
            Item item = SpawnEggItem.forEntity((EntityType<?>) entry);
            if (item == null) item = Items.EGG;
            n.item(item);
        }
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

    public Item getItem() {
        return item;
    }

    public String getInformation() {
        return information;
    }

    public Node withInformation(String information) {
        this.information = information;
        return this;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public Node item(Item item) {
        this.item = item;
        return this;
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

    public String getName() {
        return getId();
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
                if (node.getId().equals(id)) {
                    String s = input.contains(".") ? input.substring(input.indexOf('.') + 1) : "";
                    List<Node> list = node.getNodes(s);
                    list.add(node);
                    return list;
                }
            }
        }
        throw new InvalidPermissionException("Couldn't find " + id + " node in " + this.getId());
    }
}
