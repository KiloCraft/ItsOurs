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
import me.drex.itsours.claim.permission.util.Misc;
import me.drex.itsours.claim.permission.util.Modify;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ButtonBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class PermissionManager {

    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();

    public static final RootNode PERMISSION = Node.root("permission").build();
    public static final RootNode SETTING = Node.root("setting").build();
    public static final RootNode COMBINED = Node.root("combined").build();

    public static final Predicate<Item> USE_ITEM_PREDICATE = item -> !overrides(item.getClass(), Item.class, DEV_ENV ? "use" : "method_7836", World.class, PlayerEntity.class, Hand.class) || item.isFood();
    public static final Predicate<Item> USE_ON_BLOCK_PREDICATE = item -> (!overrides(item.getClass(), Item.class, DEV_ENV ? "useOnBlock" : "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem);
    public static final Predicate<Block> INTERACT_BLOCK_PREDICATE = block -> (!overrides(block.getClass(), Block.class, DEV_ENV ? "onUse" : "method_9534", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class) || !overrides(block.getClass(), Block.class, DEV_ENV ? "onBlockBreakStart" : "method_9606", BlockState.class, World.class, BlockPos.class, PlayerEntity.class) || block instanceof ButtonBlock || block instanceof AbstractPressurePlateBlock);
    public static final Predicate<EntityType<?>> INTERACT_ENTITY_PREDICATE = entityType -> {
        Entity entity = entityType.create(ItsOurs.INSTANCE.server.getOverworld());
        if (entity == null) {
            return false;
        } else {
            return !overrides(entity.getClass(), Entity.class, DEV_ENV ? "interact" : "method_5688", PlayerEntity.class, Hand.class) || !overrides(entity.getClass(), Entity.class, DEV_ENV ? "interactAt" : "method_5664", PlayerEntity.class, Vec3d.class, Hand.class);
        }
    };

    public static final List<Node> BLOCK_NODES = getNodes(Registries.BLOCK);
    public static final List<Node> INTERACT_BLOCK_NODES = getNodes(Registries.BLOCK, INTERACT_BLOCK_PREDICATE);
    public static final List<Node> ITEM_BLOCK_NODES = getNodes(Registries.ITEM, USE_ON_BLOCK_PREDICATE);
    public static final List<Node> USE_ITEM_NODES = getNodes(Registries.ITEM, USE_ITEM_PREDICATE);
    public static final List<Node> DAMAGE_ENTITY_NODES = getNodes(Registries.ENTITY_TYPE, entityType -> !entityType.equals(EntityType.PLAYER));
    public static final List<Node> INTERACT_ENTITY_NODES = getNodes(Registries.ENTITY_TYPE, INTERACT_ENTITY_PREDICATE);

    public static final AbstractNode PLACE = Node.single("place")
            .description("permission.place")
            .icon(Items.MANGROVE_PLANKS)
            .then(BLOCK_NODES)
            .build();

    public static final AbstractNode MINE = Node.single("mine")
            .description("permission.mine")
            .icon(Items.NETHERITE_PICKAXE)
            .then(BLOCK_NODES)
            .build();

    public static final AbstractNode INTERACT_BLOCK = Node.single("interact_block")
            .description("permission.interact_block")
            .icon(Items.FURNACE)
            .then(INTERACT_BLOCK_NODES)
            .build();

    public static final AbstractNode USE_ON_BLOCK = Node.single("use_on_block")
            .description("permission.use_on_block")
            .icon(Items.IRON_SHOVEL)
            .then(ITEM_BLOCK_NODES)
            .build();

    public static final AbstractNode USE_ITEM = Node.single("use_item")
            .description("permission.use_item")
            .icon(Items.FIREWORK_ROCKET)
            .then(USE_ITEM_NODES)
            .build();

    public static final AbstractNode DAMAGE_ENTITY = Node.single("damage_entity")
            .description("permission.damage_entity")
            .icon(Items.DIAMOND_SWORD)
            .then(DAMAGE_ENTITY_NODES)
            .build();

    public static final AbstractNode INTERACT_ENTITY = Node.single("interact_entity")
            .description("permission.interact_entity")
            .icon(Items.VILLAGER_SPAWN_EGG)
            .then(INTERACT_ENTITY_NODES)
            .build();

    public static final AbstractNode MISC = Node.single("misc")
            .description("permission.misc")
            .icon(Items.ELYTRA)
            .then(Arrays.stream(Misc.values()).map(Misc::node).toList())
            .build();

    public static final AbstractNode MODIFY = Node.single("modify")
            .description("permission.modify")
            .icon(Items.REPEATER)
            .predicate(context -> context.context() != GlobalContext.INSTANCE)
            .then(Arrays.stream(Modify.values()).map(Modify::node).toList())
            .build();

    public static final AbstractNode PVP = Node.single("pvp")
            .description("setting.pvp")
            .icon(Items.BOW)
            .build();

    public static final AbstractNode EXPLOSIONS = Node.single("explosions")
            .description("setting.explosions")
            .icon(Items.TNT)
            .build();

    public static final AbstractNode FLUID_CROSSES_BORDERS = Node.single("fluid_crosses_borders")
            .description("setting.fluid_crosses_borders")
            .icon(Items.WATER_BUCKET)
            .build();

    // TODO: Description
    public static final AbstractNode SCULK_CROSSES_BORDERS = Node.single("sculk_crosses_borders")
            .description("setting.sculk_crosses_borders")
            .icon(Items.SCULK_VEIN)
            .build();

    public static final AbstractNode MOB_SPAWN = Node.single("mob_spawn")
            .description("setting.mob_spawn")
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
        registerPermission(MISC);
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
                SingleNodeBuilder builder = Node.single(formatIdentifier(identifier));
                addItem(entry.value(), builder);
                entries.add(builder.build());
            }
            if (!entries.isEmpty()) {
                GroupNodeBuilder builder = Node.group(formatIdentifier(tagKey.id()));
                builder.contained(entries);
                builder.description(Text.translatable("text.itsours.node.group.count.description", entries.size()));
                addItem(symbol, builder);
                nodes.add(builder.then(child).build());
            }
        });

        // Single nodes
        for (T entry : registry) {
            assert registry.getId(entry) != null;
            AbstractNodeBuilder builder = Node.single(formatIdentifier(registry.getId(entry))).then(child);
            addItem(entry, builder);
            if (!predicate.test(entry)) continue;
            nodes.add(builder.build());
        }
        return nodes;
    }

    private static String formatIdentifier(Identifier identifier) {
        if (identifier.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return identifier.getPath();
        }
        return identifier.toString();
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
            throw new RuntimeException("An error occurred while retrieving " + methodName + "(" + String.join(", ", Arrays.stream(classes).map(Class::getName).toList()) + ") in " + clazz1.getName() + ", " + clazz2.getName() + ", maybe the method name or parameters changed?");
        }
    }

}
