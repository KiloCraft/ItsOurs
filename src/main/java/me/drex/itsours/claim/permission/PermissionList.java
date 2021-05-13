package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.node.SettingNode;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.claim.permission.util.node.PermissionNode;
import me.drex.itsours.claim.permission.util.node.RootNode;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PermissionList {

    public static final RootNode permission = new RootNode("");
    public static final RootNode setting = new RootNode("");

    public static Predicate<Item> useItem = item -> !overrides(item.getClass(), Item.class, "method_7836", World.class, PlayerEntity.class, Hand.class) || item.isFood();
    public static Predicate<Item> useOnBlock = item -> (!overrides(item.getClass(), Item.class, "method_7884", ItemUsageContext.class)) && !(item instanceof BlockItem) ;
    public static Predicate<Block> interactBlock = block -> (!overrides(block.getClass(), Block.class, "method_9534", BlockState.class, World.class, BlockPos.class, PlayerEntity.class, Hand.class, BlockHitResult.class) || block instanceof AbstractButtonBlock || block instanceof AbstractPressurePlateBlock);

    public static void register() {

        List<Node> blockNodes = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup());
        registerPermission((PermissionNode) new PermissionNode("place").withInformation("Place blocks").addNodes(blockNodes));
        registerPermission((PermissionNode) new PermissionNode("mine").withInformation("Mine blocks").addNodes(blockNodes));

        List<Node> interactableBlockNodes = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup(), interactBlock);
        registerPermission((PermissionNode) new PermissionNode("interact_block").withInformation("Rightclick on blocks").addNodes(interactableBlockNodes));

        List<Node> itemBlockNodes = Node.getNodes(Registry.ITEM, ItemTags.getTagGroup(), useOnBlock);
        registerPermission((PermissionNode) new PermissionNode("use_on_block").withInformation("Use an item on a block").addNodes(itemBlockNodes));

        List<Node> useItemNodes = Node.getNodes(Registry.ITEM, ItemTags.getTagGroup(), useItem);
        registerPermission((PermissionNode) new PermissionNode("use_item").withInformation("Rightclick with an item").addNodes(useItemNodes));

        List<Node> entityNodes = Node.getNodes(Registry.ENTITY_TYPE, EntityTypeTags.getTagGroup()).stream().filter(node -> !node.getId().equals("player")).collect(Collectors.toList());
        registerPermission((PermissionNode) new PermissionNode("damage_entity").withInformation("Hit / damage entities").addNodes(entityNodes));
        registerPermission((PermissionNode) new PermissionNode("interact_entity").withInformation("Rightclick on entities").addNodes(entityNodes));

        registerPermission((PermissionNode) new PermissionNode("modify").withInformation("Claim permissions").addSimpleNodes(Arrays.asList("trust", "untrust", "distrust", "size", "permission", "setting", "subzone", "name")));

        registerSetting((SettingNode) new SettingNode("pvp").global().withInformation("Toggle Player vs Player"));
        registerSetting((SettingNode) new SettingNode("mobspawn").global().withInformation("Toggle mobspawning"));
        registerSetting((SettingNode) new SettingNode("explosions").withInformation("Toggle explosion block damage"));
        registerSetting((SettingNode) new SettingNode("fluid_crosses_borders").withInformation("Toggle fluids crossing claim borders"));

    }

    private static boolean overrides(Class<?> clazz1, Class<?> clazz2, String methodName, Class<?>... classes) {
        try {
            return clazz1.getMethod(methodName, classes).equals(clazz2.getMethod(methodName, classes));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("An error occured while retrieving " + methodName + " in " + clazz1.getName() + " or " + clazz2.getName() + ", maybe the method name or parameters changed?");
        }
    }

    public static <T> boolean filter(T entry, Predicate<T> p) {
        return p.test(entry);
    }

    public static void registerPermission(PermissionNode node) {
        permission.add(node);
    }

    public static void registerSetting(SettingNode node) {
        setting.add(node);
    }

}
