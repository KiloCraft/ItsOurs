package me.drex.itsours.claim.permission.util;

import com.google.common.collect.Lists;
import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.claim.permission.util.node.GroupNode;
import me.drex.itsours.claim.permission.util.node.SingleNode;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.*;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class Group {
    public static final Group BLOCK = create("block", Registry.BLOCK, BlockTags.getTagGroup());
    public static Predicate<Item> useItem = item -> !overrides(item.getClass(), Item.class, "method_7836", World.class, PlayerEntity.class, Hand.class) || item.isFood();
    public static Predicate<Item> useOnBlock = item -> (!overrides(item.getClass(), Item.class, "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem) ;
    public static Predicate<Block> interactBlock = block -> (!overrides(block.getClass(), Block.class, "method_9534", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class) || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock);
    public static final Group USE_ITEM = create("item", Registry.ITEM, ItemTags.getTagGroup(), useItem);
    public static final Group USE_ON_BLOCKS = create("item", Registry.ITEM, ItemTags.getTagGroup(), useOnBlock);
    public static final Group INTERACTABLE_BLOCKS = create("block", Registry.BLOCK, BlockTags.getTagGroup(), interactBlock);
    public static final Group ENTITY = create("entity", Registry.ENTITY_TYPE, EntityTypeTags.getTagGroup());
    public static final Group ITEMS = create("item", Registry.ITEM, ItemTags.getTagGroup());
    public static final Group MODIFY = create("permission", "trust", "distrust", "size", "permission", "setting", "subzone", "name");
    public final List<AbstractNode> list;
    public final String id;

    public Group(String id, final List<AbstractNode> list) {
        this.id = id;
        this.list = list;
    }

    public static <T> Group create(String id, @NotNull final Registry<T> registry, TagGroup<T> tagGroup) {
        return create(id, registry, tagGroup, t -> true);
    }

        public static <T> Group create(String id, @NotNull final Registry<T> registry, TagGroup<T> tagGroup, Predicate<T> p) {
        Validate.notNull(registry, "Registry must not be null!");
        Validate.notNull(tagGroup, "Identified tag list must not be null!");
        final List<AbstractNode> nodes = Lists.newArrayList();
        for (Map.Entry<Identifier, Tag<T>> mapEntry : tagGroup.getTags().entrySet()) {
            Tag<T> tag = mapEntry.getValue();
            final List<String> entries = Lists.newArrayList();

            for (T entry : registry) {
                if (!p.test(entry)) continue;
                if (tag.contains(entry)) {
                    final Identifier identifier = registry.getId(entry);
                    Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.toString());
                    entries.add(identifier.getPath());
                }
            }
            if (!entries.isEmpty())
                nodes.add(new GroupNode(mapEntry.getKey().getPath().toUpperCase(Locale.ENGLISH), entries.toArray(new String[0])));
        }
        for (T entry : registry) {
            if (!p.test(entry)) continue;
            nodes.add(new SingleNode(
                    Validate.notNull(
                            registry.getId(entry),
                            "%s does not contain entry %s", registry.toString(), entry.toString()
                    ).getPath()
            ));
        }
        return new Group(id, nodes);
    }

    private static boolean overrides(Class<?> clazz1, Class<?> clazz2, String methodName, Class<?>... classes) {
        try {
            return clazz1.getMethod(methodName, classes).equals(clazz2.getMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("An error occured while retrieving " + methodName + " in " + clazz1.getName() + " or " + clazz2.getName() + ", maybe the method name or parameters changed?");
        }
    }

    public static Group create(String id, String... entries) {
        final List<AbstractNode> nodes = Lists.newArrayList();
        for (String entry : entries) {
            nodes.add(new SingleNode(entry));
        }
        return new Group(id, nodes);
    }

    public static <T> boolean filter(T entry, Class<?>... filter) {
        if (filter.length > 0) {
            for (Class<?> clazz : filter) {
                if (clazz.isInstance(entry)) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    public static <T> boolean filter(T entry, Predicate<T> p) {
        return p.test(entry);
    }
}