package me.drex.itsours.claim.flags.util;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import me.drex.itsours.claim.flags.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.flags.node.builder.LiteralNodeBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

public class FlagBuilderUtil {
    public static final Map<EntityType<?>, ItemStack> ENTITY_PICK_BLOCK_STATE = new HashMap<>();

    public static final Predicate<Item> USE_ITEM_PREDICATE = item -> overrides(item.getClass(), Item.class, "use", Level.class, Player.class, InteractionHand.class) || item.components().has(DataComponents.FOOD);
    public static final Predicate<Item> USE_ON_BLOCK_PREDICATE = item -> (overrides(item.getClass(), Item.class, "useOn", UseOnContext.class)) && !(item instanceof BlockItem);
    public static final Predicate<Block> INTERACT_BLOCK_PREDICATE = block -> {
        boolean onUseWithItem = overrides(block.getClass(), Block.class, "useItemOn", ItemStack.class, BlockState.class, Level.class, BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class);
        boolean onUse = overrides(block.getClass(), Block.class, "useWithoutItem", BlockState.class, Level.class, BlockPos.class, Player.class, BlockHitResult.class);
        // Instant-mine interactions (dragon egg, note block and redstone ore)
        boolean onBlockBreakStart = overrides(block.getClass(), Block.class, "attack", BlockState.class, Level.class, BlockPos.class, Player.class);
        return !(block instanceof StairBlock) &&
            (onUseWithItem || onUse || onBlockBreakStart ||
                block instanceof ButtonBlock || block instanceof BasePressurePlateBlock);
    };

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
                try {
                    Entity dummy = entityType.create(server.overworld(), EntitySpawnReason.LOAD);
                    if (dummy == null) continue;
                    ItemStack pickBlockStack = dummy.getPickResult();
                    if (pickBlockStack == null) {
                        if (dummy instanceof ItemSupplier flyingItemEntity) {
                            pickBlockStack = flyingItemEntity.getItem();
                        } else if (dummy instanceof AbstractArrow projectileEntity) {
                            pickBlockStack = projectileEntity.getPickupItemStackOrigin();
                        }
                    }
                    ENTITY_PICK_BLOCK_STATE.put(entityType, pickBlockStack);
                } catch (Exception ignored) {
                }
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
        registry.getTags().forEach(registryEntries -> {
            GroupNodeBuilder builder = new GroupNodeBuilder(registry, registryEntries.key(), entryPredicate);
            builder.then(child);
            GroupNode groupNode = builder.build();
            if (groupNode.getContained().size() > 1) {
                nodes.add(groupNode);
            }
        });

        // Single nodes
        for (T entry : registry) {
            assert registry.getKey(entry) != null;
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
