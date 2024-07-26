package me.drex.itsours.claim.flags.util;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.node.builder.AbstractNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.LiteralNodeBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class FlagBuilderUtil {
    public static final Map<EntityType<?>, ItemStack> ENTITY_PICK_BLOCK_STATE = new HashMap<>();

    public static final Predicate<Item> USE_ITEM_PREDICATE = item -> overrides(item.getClass(), Item.class, Flags.DEV_ENV ? "use" : "method_7836", World.class, PlayerEntity.class, Hand.class) || item.getComponents().contains(DataComponentTypes.FOOD);
    public static final Predicate<Item> USE_ON_BLOCK_PREDICATE = item -> (overrides(item.getClass(), Item.class, Flags.DEV_ENV ? "useOnBlock" : "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem);
    public static final Predicate<Block> INTERACT_BLOCK_PREDICATE = block -> {
        boolean onUseWithItem = overrides(block.getClass(), Block.class, Flags.DEV_ENV ? "onUseWithItem" : "method_55765", ItemStack.class, BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class);
        boolean onUse = overrides(block.getClass(), Block.class, Flags.DEV_ENV ? "onUse" : "method_55766", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, BlockHitResult.class);
        // Instant-mine interactions (dragon egg, note block and redstone ore)
        boolean onBlockBreakStart = overrides(block.getClass(), Block.class, Flags.DEV_ENV ? "onBlockBreakStart" : "method_9606", BlockState.class, World.class, BlockPos.class, PlayerEntity.class);
        return !(block instanceof StairsBlock) &&
            (onUseWithItem || onUse || onBlockBreakStart ||
                block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock);
    };

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (EntityType<?> entityType : Registries.ENTITY_TYPE) {
                Entity dummy = entityType.create(server.getOverworld());
                if (dummy == null) continue;
                ItemStack pickBlockStack = dummy.getPickBlockStack();
                if (pickBlockStack == null) {
                    if (dummy instanceof FlyingItemEntity flyingItemEntity) {
                        pickBlockStack = flyingItemEntity.getStack();
                    } else if (dummy instanceof PersistentProjectileEntity projectileEntity) {
                        pickBlockStack = projectileEntity.getItemStack();
                    }
                }
                ENTITY_PICK_BLOCK_STATE.put(entityType, pickBlockStack);
            }
            Flags.register();
        });
    }

    public static <T> List<ChildNode> getNodes(@NotNull final Registry<T> registry) {
        return getNodes(registry, Collections.emptyList(), entryPredicate -> true);
    }

    public static <T> List<ChildNode> getNodes(@NotNull final Registry<T> registry, Predicate<T> entryPredicate) {
        return getNodes(registry, Collections.emptyList(), entryPredicate);
    }

    private static <T> List<ChildNode> getNodes(@NotNull final Registry<T> registry, List<ChildNode> child, Predicate<T> entryPredicate) {
        List<ChildNode> nodes = new LinkedList<>();
        // Group nodes
        registry.streamTags().forEach(tagKey -> {
            GroupNodeBuilder builder = new GroupNodeBuilder(registry, tagKey, entryPredicate);
            builder.then(child);
            GroupNode groupNode = builder.build();
            if (groupNode.getContained().size() > 1) {
                nodes.add(groupNode);
            }
        });

        // Single nodes
        for (T entry : registry) {
            assert registry.getId(entry) != null;
            LiteralNodeBuilder builder = new LiteralNodeBuilder(registry, entry);
            builder.then(child);
            if (!entryPredicate.test(entry)) continue;
            nodes.add(builder.build());
        }
        return nodes;
    }

    private static boolean overrides(Class<?> clazz1, Class<?> clazz2, String methodName, Class<?>... classes) {
        try {
            Method method1 = findMethod(clazz1, methodName, classes);
            Method method2 = findMethod(clazz2, methodName, classes);
            return !method1.equals(method2);
        } catch (NoSuchMethodException e) {
            ItsOurs.LOGGER.error("Failed to retrieve method {}({}) in {} or {}", methodName, String.join(", ", Arrays.stream(classes).map(Class::getName).toList()), clazz1.getName(), clazz2.getName());
            ItsOurs.LOGGER.error("Method candidates for {}:", clazz1.toString());
            logMethodCandidates(clazz1, methodName);
            ItsOurs.LOGGER.error("Method candidates for {}:", clazz2.toString());
            logMethodCandidates(clazz2, methodName);
            throw new RuntimeException(e);
        }
    }

    private static Method findMethod(Class<?> clazz, String methodName, Class<?>... classes) throws NoSuchMethodException {
        try {
            return clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass != null) {
                return findMethod(superClass, methodName, classes);
            } else {
                throw e;
            }
        }
    }

    private static void logMethodCandidates(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                ItsOurs.LOGGER.error("{}", method.toString());
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            logMethodCandidates(superClass, methodName);
        }
    }
}
