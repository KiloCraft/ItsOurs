package me.drex.itsours.claim.permission.rework;

import com.google.common.collect.Lists;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.rework.context.GlobalContext;
import me.drex.itsours.claim.permission.rework.node.AbstractNode;
import me.drex.itsours.claim.permission.rework.node.Node;
import me.drex.itsours.claim.permission.rework.node.RootNode;
import me.drex.itsours.claim.permission.rework.node.builder.AbstractNodeBuilder;
import me.drex.itsours.claim.permission.rework.node.builder.GroupNodeBuilder;
import me.drex.itsours.claim.permission.rework.node.builder.SingleNodeBuilder;
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

    public static final Predicate<Item> USE_ITEM = item -> !overrides(item.getClass(), Item.class, "method_7836", World.class, PlayerEntity.class, Hand.class) || item.isFood();
    public static final Predicate<Item> USE_ON_BLOCK = item -> (!overrides(item.getClass(), Item.class, "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem);
    public static final Predicate<Block> INTERACT_BLOCK = block -> (!overrides(block.getClass(), Block.class, "method_9534", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class) || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock);
    public static final Predicate<EntityType<?>> INTERACT_ENTITY = entityType -> {
        Entity entity = entityType.create(ItsOurs.INSTANCE.server.getOverworld());
        if (entity == null) {
            return false;
        } else {
            return !overrides(entity.getClass(), Entity.class, "method_5688", PlayerEntity.class, Hand.class) || !overrides(entity.getClass(), Entity.class, "method_5664", PlayerEntity.class, Vec3d.class, Hand.class);
        }
    };

    // TODO: Put all nodes like this
    public static final AbstractNode MODIFY = Node.single("modify")
            .description("text.itsours.permission.modify.description")
            .icon(Items.REPEATER)
            .predicate(context -> context.context() != GlobalContext.INSTANCE)
            .then(Arrays.stream(Modify.values()).map(Modify::buildNode).toList())
            .build();

    public static void register() {
        // place
        List<Node> blockNodes = getNodes(Registry.BLOCK);
        final AbstractNodeBuilder PLACE = Node.single("place")
                .description("text.itsours.permission.place.description")
                .icon(Items.MANGROVE_PLANKS)
                .then(blockNodes);
        registerPermission(PLACE.build());
        // mine
        final AbstractNodeBuilder MINE = Node.single("mine")
                .description("text.itsours.permission.mine.description")
                .icon(Items.NETHERITE_PICKAXE)
                .then(blockNodes);
        registerPermission(MINE.build());
        // interact_block
        List<Node> interactableBlockNodes = getNodes(Registry.BLOCK, INTERACT_BLOCK);
        final AbstractNodeBuilder INTERACT_BLOCK = Node.single("interact_block")
                .description("text.itsours.permission.interact_block.description")
                .icon(Items.FURNACE)
                .then(interactableBlockNodes);
        registerPermission(INTERACT_BLOCK.build());
        // use_on_block
        List<Node> itemBlockNodes = getNodes(Registry.ITEM, USE_ON_BLOCK);
        final AbstractNodeBuilder USE_ON_BLOCK = Node.single("use_on_block")
                .description("text.itsours.permission.use_on_block.description")
                .icon(Items.IRON_SHOVEL)
                .then(itemBlockNodes);
        registerPermission(USE_ON_BLOCK.build());
        // use_item
        List<Node> useItemNodes = getNodes(Registry.ITEM, USE_ITEM);
        final AbstractNodeBuilder USE_ITEM = Node.single("use_item")
                .description("text.itsours.permission.use_item.description")
                .icon(Items.FIREWORK_ROCKET)
                .then(useItemNodes);
        registerPermission(USE_ITEM.build());
        // damage_entity
        List<Node> damegeEntitiesNode = getNodes(Registry.ENTITY_TYPE, entityType -> !entityType.equals(EntityType.PLAYER));
        final AbstractNodeBuilder DAMAGE_ENTITY = Node.single("damage_entity")
                .description("text.itsours.permission.damage_entity.description")
                .icon(Items.DIAMOND_SWORD)
                .then(damegeEntitiesNode);
        registerPermission(DAMAGE_ENTITY.build());
        // interact_entity
        List<Node> interactableEntityNodes = getNodes(Registry.ENTITY_TYPE, INTERACT_ENTITY);
        final AbstractNodeBuilder INTERACT_ENTITY = Node.single("interact_entity")
                .description("text.itsours.permission.interact_entity.description")
                .icon(Items.VILLAGER_SPAWN_EGG)
                .then(interactableEntityNodes);
        registerPermission(INTERACT_ENTITY.build());
        // modify
        registerPermission(MODIFY);

        final AbstractNodeBuilder PVP = Node.single("pvp")
                .description("text.itsours.setting.pvp.description")
                .icon(Items.BOW);
        registerSetting(PVP.build());
        final AbstractNodeBuilder EXPLOSION = Node.single("explosions")
                .description("text.itsours.setting.explosions.description")
                .icon(Items.TNT);
        registerSetting(EXPLOSION.build());
        final AbstractNodeBuilder FLUID_CROSSES_BORDERS = Node.single("fluid_crosses_borders")
                .description("text.itsours.setting.fluid_crosses_borders.description")
                .icon(Items.WATER_BUCKET);
        registerSetting(FLUID_CROSSES_BORDERS.build());
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
                addItem(entry, builder);
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
