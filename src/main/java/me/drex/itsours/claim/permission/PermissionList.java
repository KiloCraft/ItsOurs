package me.drex.itsours.claim.permission;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.util.node.PermissionNode;
import me.drex.itsours.claim.permission.util.node.RootNode;
import me.drex.itsours.claim.permission.util.node.SettingNode;
import me.drex.itsours.claim.permission.util.node.util.Node;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PermissionList {

    public final RootNode permission = new RootNode("permission");
    public final RootNode setting = new RootNode("setting");
    public final RootNode both = new RootNode("setting");

    public static final PermissionList INSTANCE = new PermissionList();

    private PermissionList() {
    }

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

    public void register() {
        List<Node> blockNodes = Node.getNodes(Registry.BLOCK);
        registerPermission((PermissionNode) new PermissionNode("place").withInformation("Place blocks").addNodes(blockNodes).item(Items.STONE));
        registerPermission((PermissionNode) new PermissionNode("mine").withInformation("Mine blocks").addNodes(blockNodes).item(Items.DIAMOND_PICKAXE));

        List<Node> interactableBlockNodes = Node.getNodes(Registry.BLOCK, INTERACT_BLOCK);
        registerPermission((PermissionNode) new PermissionNode("interact_block").withInformation("Rightclick on blocks").addNodes(interactableBlockNodes).item(Items.FURNACE));

        List<Node> itemBlockNodes = Node.getNodes(Registry.ITEM, USE_ON_BLOCK);
        registerPermission((PermissionNode) new PermissionNode("use_on_block").withInformation("Use an item on a block").addNodes(itemBlockNodes).item(Items.IRON_SHOVEL));

        List<Node> useItemNodes = Node.getNodes(Registry.ITEM, USE_ITEM);
        registerPermission((PermissionNode) new PermissionNode("use_item").withInformation("Rightclick with an item").addNodes(useItemNodes).item(Items.FIREWORK_ROCKET));

        List<Node> entityNodes = Node.getNodes(Registry.ENTITY_TYPE).stream().filter(node -> !node.getId().equals("player")).collect(Collectors.toList());
        registerPermission((PermissionNode) new PermissionNode("damage_entity").withInformation("Hit / damage entities").addNodes(entityNodes).item(Items.DIAMOND_SWORD));

        List<Node> interactableEntityNodes = Node.getNodes(Registry.ENTITY_TYPE, INTERACT_ENTITY);
        registerPermission((PermissionNode) new PermissionNode("interact_entity").withInformation("Rightclick on entities").addNodes(interactableEntityNodes).item(Items.VILLAGER_SPAWN_EGG));

        registerPermission((PermissionNode) new PermissionNode("modify").withInformation("Claim permissions").addSimpleNodes(Arrays.asList("trust", "untrust", "distrust", "size", "permission", "setting", "subzone", "name", "role")).item(Items.REPEATER));

        registerSetting((SettingNode) new SettingNode("pvp").global().withInformation("Toggle Player vs Player").item(Items.BOW));
        registerSetting((SettingNode) new SettingNode("explosions").withInformation("Toggle explosion block damage").item(Items.TNT));
        registerSetting((SettingNode) new SettingNode("fluid_crosses_borders").withInformation("Toggle fluids crossing claim borders").item(Items.WATER_BUCKET));

    }

    private static boolean overrides(Class<?> clazz1, Class<?> clazz2, String methodName, Class<?>... classes) {
        try {
            return clazz1.getMethod(methodName, classes).equals(clazz2.getMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("An error occurred while retrieving " + methodName + " in " + clazz1.getName() + " or " + clazz2.getName() + ", maybe the method name or parameters changed?");
        }
    }

    public static <T> boolean filter(T entry, Predicate<T> p) {
        return p.test(entry);
    }

    private void registerPermission(PermissionNode node) {
        permission.add(node);
        both.add(node);
    }

    private void registerSetting(SettingNode node) {
        setting.add(node);
        both.add(node);
    }

}
