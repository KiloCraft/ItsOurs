package me.drex.itsours.claim.permission;

import com.google.common.collect.Lists;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.context.GlobalContext;
import me.drex.itsours.claim.permission.node.AbstractNode;
import me.drex.itsours.claim.permission.node.Node;
import me.drex.itsours.claim.permission.node.RootNode;
import me.drex.itsours.claim.permission.node.builder.AbstractNodeBuilder;
import me.drex.itsours.claim.permission.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.permission.node.builder.SingleNodeBuilder;
import me.drex.itsours.claim.permission.util.Modify;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class PermissionManager {

    public static final RootNode PERMISSION = Node.root("permission").build();
    public static final RootNode SETTING = Node.root("setting").build();
    public static final RootNode COMBINED = Node.root("combined").build();

    public static final Predicate<Item> USE_ITEM_PREDICATE = item -> !overrides(item.getClass(), Item.class, "method_7836", World.class, PlayerEntity.class, Hand.class) || item.isFood();
    public static final Predicate<Item> USE_ON_BLOCK_PREDICATE = item -> (!overrides(item.getClass(), Item.class, "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem);
    public static final Predicate<Block> INTERACT_BLOCK_PREDICATE = block -> (!overrides(block.getClass(), Block.class, "method_9534", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class) || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock);
    public static final Predicate<EntityType<?>> INTERACT_ENTITY_PREDICATE = entityType -> {
        Entity entity = entityType.create(ItsOurs.INSTANCE.server.getOverworld());
        if (entity == null) {
            return false;
        } else {
            return !overrides(entity.getClass(), Entity.class, "method_5688", PlayerEntity.class, Hand.class) || !overrides(entity.getClass(), Entity.class, "method_5664", PlayerEntity.class, Vec3d.class, Hand.class);
        }
    };

    public static final List<Node> BLOCK_NODES = getNodes(Registry.BLOCK);
    public static final List<Node> INTERACT_BLOCK_NODES = getNodes(Registry.BLOCK, INTERACT_BLOCK_PREDICATE);
    public static final List<Node> ITEM_BLOCK_NODES = getNodes(Registry.ITEM, USE_ON_BLOCK_PREDICATE);
    public static final List<Node> USE_ITEM_NODES = getNodes(Registry.ITEM, USE_ITEM_PREDICATE);
    public static final List<Node> DAMAGE_ENTITY_NODES = getNodes(Registry.ENTITY_TYPE, entityType -> !entityType.equals(EntityType.PLAYER));
    public static final List<Node> INTERACT_ENTITY_NODES = getNodes(Registry.ENTITY_TYPE, INTERACT_ENTITY_PREDICATE);

    public static final AbstractNode PLACE = Node.single("place")
            .description("text.itsours.permission.place.description")
            .icon(Items.MANGROVE_PLANKS)
            .then(BLOCK_NODES)
            .build();

    public static final AbstractNode MINE = Node.single("mine")
            .description("text.itsours.permission.mine.description")
            .icon(Items.NETHERITE_PICKAXE)
            .then(BLOCK_NODES)
            .build();

    public static final AbstractNode INTERACT_BLOCK = Node.single("interact_block")
            .description("text.itsours.permission.interact_block.description")
            .icon(Items.FURNACE)
            .then(INTERACT_BLOCK_NODES)
            .build();

    public static final AbstractNode USE_ON_BLOCK = Node.single("use_on_block")
            .description("text.itsours.permission.use_on_block.description")
            .icon(Items.IRON_SHOVEL)
            .then(ITEM_BLOCK_NODES)
            .build();

    public static final AbstractNode USE_ITEM = Node.single("use_item")
            .description("text.itsours.permission.use_item.description")
            .icon(Items.FIREWORK_ROCKET)
            .then(USE_ITEM_NODES)
            .build();

    public static final AbstractNode DAMAGE_ENTITY = Node.single("damage_entity")
            .description("text.itsours.permission.damage_entity.description")
            .icon(Items.DIAMOND_SWORD)
            .then(DAMAGE_ENTITY_NODES)
            .build();

    public static final AbstractNode INTERACT_ENTITY = Node.single("interact_entity")
            .description("text.itsours.permission.interact_entity.description")
            .icon(Items.VILLAGER_SPAWN_EGG)
            .then(INTERACT_ENTITY_NODES)
            .build();

    public static final AbstractNode MODIFY = Node.single("modify")
            .description("text.itsours.permission.modify.description")
            .icon(Items.REPEATER)
            .predicate(context -> context.context() != GlobalContext.INSTANCE)
            .then(Arrays.stream(Modify.values()).map(Modify::buildNode).toList())
            .build();

    public static final AbstractNode PVP = Node.single("pvp")
            .description("text.itsours.setting.pvp.description")
            .icon(Items.BOW)
            .build();

    public static final AbstractNode EXPLOSIONS = Node.single("explosions")
            .description("text.itsours.setting.explosions.description")
            .icon(Items.TNT)
            .build();

    public static final AbstractNode FLUID_CROSSES_BORDERS = Node.single("fluid_crosses_borders")
            .description("text.itsours.setting.fluid_crosses_borders.description")
            .icon(Items.WATER_BUCKET)
            .build();

    // TODO: Description
    public static final AbstractNode SCULK_CROSSES_BORDERS = Node.single("sculk_crosses_borders")
            .description("text.itsours.setting.todo.description")
            .icon(Items.SCULK_VEIN)
            .build();

    public static final AbstractNode MOB_SPAWN = Node.single("mob_spawn")
            .description("text.itsours.setting.mob_spawn.description")
            .icon(Items.ZOMBIE_SPAWN_EGG)
            .predicate(changeContext -> ItsOurs.hasPermission(changeContext.source(), "itsours.mob_spawn"))
            .build();

    public static void register() {
        registerPermission(PLACE);
        registerPermission(MINE);
        registerPermission(INTERACT_BLOCK);
        registerPermission(USE_ON_BLOCK);
        registerPermission(USE_ITEM);
        registerPermission(DAMAGE_ENTITY);
        registerPermission(INTERACT_ENTITY);
        registerPermission(MODIFY);
        // Settings
        registerSetting(PVP);
        registerSetting(EXPLOSIONS);
        registerSetting(FLUID_CROSSES_BORDERS);
        registerSetting(SCULK_CROSSES_BORDERS);
        registerSetting(MOB_SPAWN);
    }

    private static void registerPermission(Node node) {
        PERMISSION.addNode(node);
        COMBINED.addNode(node);
    }

    private static void registerSetting(Node node) {
        SETTING.addNode(node);
        COMBINED.addNode(node);
    }

    private static <T> List<Node> getNodes(@NotNull final Registry<T> registry) {
        return getNodes(registry, Collections.emptyList(), predicate -> true);
    }

    private static <T> List<Node> getNodes(@NotNull final Registry<T> registry, Predicate<T> predicate) {
        return getNodes(registry, Collections.emptyList(), predicate);
    }

    private static <T> List<Node> getNodes(@NotNull final Registry<T> registry, List<Node> child, Predicate<T> predicate) {
        List<Node> nodes = new LinkedList<>();
        // Group nodes
        registry.streamTags().forEach(tagKey -> {
            final List<Node> entries = Lists.newArrayList();
            T symbol = null;
            for (RegistryEntry<T> entry : registry.iterateEntries(tagKey)) {
                if (!predicate.test(entry.value())) continue;
                if (symbol == null) symbol = entry.value();
                final Identifier identifier = registry.getId(entry.value());
                Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.value().toString());
                SingleNodeBuilder builder = Node.single(identifier.getPath());
                addItem(entry.value(), builder);
                entries.add(builder.build());
            }
            if (!entries.isEmpty()) {
                GroupNodeBuilder builder = Node.group(tagKey.id().getPath());
                builder.contained(entries);
                addItem(symbol, builder);
                nodes.add(builder.then(child).build());
            }
        });

        // Single nodes
        for (T entry : registry) {
            assert registry.getId(entry) != null;
            AbstractNodeBuilder builder = Node.single(registry.getId(entry).getPath()).then(child);
            addItem(entry, builder);
            if (!predicate.test(entry)) continue;
            nodes.add(builder.build());
        }
        return nodes;
    }

    private static <T> void addItem(T entry, AbstractNodeBuilder builder) {
        if (entry instanceof ItemConvertible convertible) {
            if (convertible.asItem().equals(Items.AIR)) return;
            builder.icon(convertible.asItem());
        } else if (entry instanceof EntityType entityType) {
            Item item = SpawnEggItem.forEntity(entityType);
            if (item == null) item = Items.EGG;
            builder.icon(item);
        }
    }

    private static boolean overrides(Class<?> clazz1, Class<?> clazz2, String methodName, Class<?>... classes) {
        try {
            return clazz1.getMethod(methodName, classes).equals(clazz2.getMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while retrieving " + methodName + " in " + clazz1.getName() + " or " + clazz2.getName() + ", maybe the method name or parameters changed?");
        }
    }

}
