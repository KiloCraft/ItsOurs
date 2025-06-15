package me.drex.itsours.claim.flags;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.flags.context.GlobalContext;
import me.drex.itsours.claim.flags.node.AbstractChildNode;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.RootNode;
import me.drex.itsours.claim.flags.util.FlagBuilderUtil;
import me.drex.itsours.claim.flags.util.Modify;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.Arrays;

import static me.drex.itsours.claim.flags.node.Node.literal;

public class Flags {

    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();

    public static final RootNode PLAYER = new RootNode("player");
    public static final RootNode GLOBAL = new RootNode("global");

    // Player flags
    public static final AbstractChildNode USE_ITEM = literal("use_item")
        .description("use_item")
        .icon(Items.FIREWORK_ROCKET)
        .then(FlagBuilderUtil.getNodes(Registries.ITEM, FlagBuilderUtil.USE_ITEM_PREDICATE))
        .build();
    public static final AbstractChildNode USE_ON_BLOCK = literal("use_on_block")
        .description("use_on_block")
        .icon(Items.IRON_SHOVEL)
        .then(FlagBuilderUtil.getNodes(Registries.ITEM, FlagBuilderUtil.USE_ON_BLOCK_PREDICATE))
        .build();
    public static final AbstractChildNode INTERACT_BLOCK = literal("interact_block")
        .description("interact_block")
        .icon(Items.FURNACE)
        .then(FlagBuilderUtil.getNodes(Registries.BLOCK, FlagBuilderUtil.INTERACT_BLOCK_PREDICATE))
        .build();
    public static final AbstractChildNode INTERACT_ENTITY = literal("interact_entity")
        .description("interact_entity")
        .icon(Items.VILLAGER_SPAWN_EGG)
        .then(FlagBuilderUtil.getNodes(Registries.ENTITY_TYPE))
        .build();
    public static final AbstractChildNode PLACE = literal("place")
        .description("place")
        .icon(Items.MANGROVE_PLANKS)
        .then(FlagBuilderUtil.getNodes(Registries.BLOCK))
        .build();
    public static final AbstractChildNode MINE = literal("mine")
        .description("mine")
        .icon(Items.NETHERITE_PICKAXE)
        .then(FlagBuilderUtil.getNodes(Registries.BLOCK))
        .build();
    public static final AbstractChildNode DAMAGE_ENTITY = literal("damage_entity")
        .description("damage_entity")
        .icon(Items.DIAMOND_SWORD)
        .then(FlagBuilderUtil.getNodes(Registries.ENTITY_TYPE, entityType -> !entityType.equals(EntityType.PLAYER)))
        .build();
    public static final AbstractChildNode GLIDE = literal("glide")
        .description("glide")
        .icon(Items.ELYTRA)
        .build();
    public static final AbstractChildNode CLAIM_FLY = literal("claim_fly")
        .description("claim_fly")
        .icon(Items.FEATHER)
        .build();

    public static final AbstractChildNode MODIFY = literal("modify")
        .description("modify")
        .icon(Items.REPEATER)
        .predicate(context -> context.context() != GlobalContext.INSTANCE)
        .then(Arrays.stream(Modify.values()).map(Modify::node).toList())
        .build();

    // Global flags
    public static final AbstractChildNode PVP = literal("pvp")
        .description("pvp")
        .icon(Items.BOW)
        .build();

    public static final AbstractChildNode EXPLOSIONS = literal("explosions")
        .description("explosions")
        .icon(Items.TNT)
        .build();

    public static final AbstractChildNode FLUID_CROSSES_BORDERS = literal("fluid_crosses_borders")
        .description("fluid_crosses_borders")
        .icon(Items.WATER_BUCKET)
        .build();

    public static final AbstractChildNode PISTON_CROSSES_BORDERS = literal("piston_crosses_borders")
        .description("piston_crosses_borders")
        .icon(Items.PISTON)
        .build();

    public static final AbstractChildNode SCULK_CROSSES_BORDERS = literal("sculk_crosses_borders")
        .description("sculk_crosses_borders")
        .icon(Items.SCULK_VEIN)
        .build();

    public static final AbstractChildNode MOB_SPAWN = literal("mob_spawn")
        .description("mob_spawn")
        .icon(Items.ZOMBIE_SPAWN_EGG)
        .predicate(changeContext -> ItsOurs.checkPermission(changeContext.source(), "itsours.setting.mob_spawn", 2))
        .build();

    public static void register() {
        // Player flags
        registerPlayerFlag(PLACE);
        registerPlayerFlag(MINE);
        registerPlayerFlag(INTERACT_BLOCK);
        registerPlayerFlag(USE_ON_BLOCK);
        registerPlayerFlag(USE_ITEM);
        registerPlayerFlag(DAMAGE_ENTITY);
        registerPlayerFlag(INTERACT_ENTITY);
        registerPlayerFlag(MODIFY);
        registerPlayerFlag(GLIDE);
        registerPlayerFlag(CLAIM_FLY);
        // Global flags
        registerClaimFlag(PVP);
        registerClaimFlag(EXPLOSIONS);
        registerClaimFlag(FLUID_CROSSES_BORDERS);
        registerClaimFlag(PISTON_CROSSES_BORDERS);
        registerClaimFlag(SCULK_CROSSES_BORDERS);
        registerClaimFlag(MOB_SPAWN);
    }

    private static void registerPlayerFlag(ChildNode node) {
        PLAYER.registerNode(node);
        GLOBAL.registerNode(node);
    }

    private static void registerClaimFlag(ChildNode node) {
        GLOBAL.registerNode(node);
    }

}
