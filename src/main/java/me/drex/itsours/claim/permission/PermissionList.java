package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.newNode.SettingNode;
import me.drex.itsours.claim.permission.util.newNode.util.Node;
import me.drex.itsours.claim.permission.util.newNode.PermissionNode;
import me.drex.itsours.claim.permission.util.newNode.RootNode;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;

public class PermissionList {

    public static final RootNode permission = new RootNode("permission");
    public static final RootNode setting = new RootNode("setting");

    public static final Class<?>[] INTERACT_BLOCK_FILTER = {BlockWithEntity.class, AbstractButtonBlock.class, AbstractCandleBlock.class, AbstractPressurePlateBlock.class, AnvilBlock.class, BedBlock.class, CakeBlock.class, CartographyTableBlock.class, CauldronBlock.class, ComparatorBlock.class, ComposterBlock.class, CraftingTableBlock.class, DoorBlock.class, DragonEggBlock.class, EnderChestBlock.class, FenceBlock.class, FenceGateBlock.class, FlowerPotBlock.class, GrindstoneBlock.class, JigsawBlock.class, LeverBlock.class, LoomBlock.class, NoteBlock.class, PumpkinBlock.class, RedstoneOreBlock.class, RedstoneWireBlock.class, RepeaterBlock.class, RespawnAnchorBlock.class, StairsBlock.class, StonecutterBlock.class, SweetBerryBushBlock.class, TntBlock.class, TrapdoorBlock.class};
    public static final Class<?>[] USE_ON_BLOCK_FILTER = {ArmorStandItem.class, AxeItem.class, BoneMealItem.class, CompassItem.class, DebugStickItem.class, DecorationItem.class, EndCrystalItem.class, EnderEyeItem.class, FilledMapItem.class, FireChargeItem.class, FireworkItem.class, FlintAndSteelItem.class, HoeItem.class, LeadItem.class, MinecartItem.class, MusicDiscItem.class, ShovelItem.class, SpawnEggItem.class, WritableBookItem.class, WritableBookItem.class};
    public static final Class<?>[] USE_ITEM_FILTER = {ArmorItem.class, BoatItem.class, BowItem.class, BucketItem.class, ChorusFruitItem.class, CrossbowItem.class, EggItem.class, ElytraItem.class, EmptyMapItem.class, EnderEyeItem.class, EnderPearlItem.class, ExperienceBottleItem.class, FireworkItem.class, FishingRodItem.class, GlassBottleItem.class, HoneyBottleItem.class, KnowledgeBookItem.class, LilyPadItem.class, LingeringPotionItem.class, MilkBucketItem.class, OnAStickItem.class, PotionItem.class, ShieldItem.class, SnowballItem.class, SpawnEggItem.class, SplashPotionItem.class, ThrowablePotionItem.class, TridentItem.class, WritableBookItem.class, WrittenBookItem.class};

    public static void register() {

        List<Node> blockNodes = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup());
        registerPermission((PermissionNode) new PermissionNode("place").addNodes(blockNodes));
        registerPermission((PermissionNode) new PermissionNode("mine").addNodes(blockNodes));

        List<Node> interactableBlockNodes = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup(), INTERACT_BLOCK_FILTER);
        registerPermission((PermissionNode) new PermissionNode("interact_block").addNodes(interactableBlockNodes));

        List<Node> itemBlockNodes = Node.getNodes(Registry.ITEM, ItemTags.getTagGroup(), blockNodes, USE_ON_BLOCK_FILTER);
        registerPermission((PermissionNode) new PermissionNode("use_on_block").addNodes(itemBlockNodes));

        List<Node> useItemNodes = Node.getNodes(Registry.ITEM, ItemTags.getTagGroup(), USE_ITEM_FILTER);
        registerPermission((PermissionNode) new PermissionNode("use_item").addNodes(useItemNodes));

        List<Node> entityNodes = Node.getNodes(Registry.ENTITY_TYPE, EntityTypeTags.getTagGroup());
        registerPermission((PermissionNode) new PermissionNode("damage_entity").addNodes(entityNodes));
        registerPermission((PermissionNode) new PermissionNode("interact_entity").addNodes(entityNodes));

        registerPermission((PermissionNode) new PermissionNode("permission").addSimpleNodes(Arrays.asList("trust", "distrust", "size", "permission", "setting", "subzone", "name")));

        registerSetting(new SettingNode("mobspawn").global());
        registerSetting(new SettingNode("explosions"));
        registerSetting(new SettingNode("fluid_crosses_borders"));

    }

    public static void registerPermission(PermissionNode node) {
        permission.add(node);
    }

    public static void registerSetting(SettingNode node) {
        setting.add(node);
    }

}
