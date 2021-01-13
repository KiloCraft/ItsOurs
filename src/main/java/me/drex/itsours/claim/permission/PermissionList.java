package me.drex.itsours.claim.permission;

import me.drex.itsours.claim.permission.util.Group;
import me.drex.itsours.claim.permission.util.newNode.Node;
import me.drex.itsours.claim.permission.util.newNode.Permission;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.List;

public class PermissionList {

    public static final Node root = new Node("root");

    public static final Class<?>[] INTERACT_BLOCK_FILTER = {BlockWithEntity.class, AbstractButtonBlock.class, AbstractCandleBlock.class, AbstractPressurePlateBlock.class, AnvilBlock.class, BedBlock.class, CakeBlock.class, CartographyTableBlock.class, CauldronBlock.class, ComparatorBlock.class, ComposterBlock.class, CraftingTableBlock.class, DoorBlock.class, DragonEggBlock.class, EnderChestBlock.class, FenceBlock.class, FenceGateBlock.class, FlowerPotBlock.class, GrindstoneBlock.class, JigsawBlock.class, LeverBlock.class, LoomBlock.class, NoteBlock.class, PumpkinBlock.class, RedstoneOreBlock.class, RedstoneWireBlock.class, RepeaterBlock.class, RespawnAnchorBlock.class, StairsBlock.class, StonecutterBlock.class, SweetBerryBushBlock.class, TntBlock.class, TrapdoorBlock.class};
    public static final Class<?>[] USE_ON_BLOCK_FILTER = {ArmorStandItem.class, AxeItem.class, BoneMealItem.class, CompassItem.class, DebugStickItem.class, DecorationItem.class, EndCrystalItem.class, EnderEyeItem.class, FilledMapItem.class, FireChargeItem.class, FireworkItem.class, FlintAndSteelItem.class, HoeItem.class, LeadItem.class, MinecartItem.class, MusicDiscItem.class, ShovelItem.class, SpawnEggItem.class, WritableBookItem.class, WritableBookItem.class};
    public static final Class<?>[] USE_ITEM_FILTER = {ArmorItem.class, BoatItem.class, BowItem.class, BucketItem.class, ChorusFruitItem.class, CrossbowItem.class, EggItem.class, ElytraItem.class, EmptyMapItem.class, EnderEyeItem.class, EnderPearlItem.class, ExperienceBottleItem.class, FireworkItem.class, FishingRodItem.class, GlassBottleItem.class, HoneyBottleItem.class, KnowledgeBookItem.class, LilyPadItem.class, LingeringPotionItem.class, MilkBucketItem.class, OnAStickItem.class, PotionItem.class, ShieldItem.class, SnowballItem.class, SpawnEggItem.class, SplashPotionItem.class, ThrowablePotionItem.class, TridentItem.class, WritableBookItem.class, WrittenBookItem.class};
    public static final List<Node> BLOCK = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup());
    public static final List<Node> INTERACTABLE_BLOCKS = Node.getNodes(Registry.BLOCK, BlockTags.getTagGroup(), INTERACT_BLOCK_FILTER);
    public static final Node USE_ON_BLOCKS = Node.of("item", Registry.ITEM, ItemTags.getTagGroup(), USE_ON_BLOCK_FILTER);
    public static final Node USE_ITEM = Node.of("item", Registry.ITEM, ItemTags.getTagGroup(), USE_ITEM_FILTER);
    public static final Node ENTITY = Node.of("entity", Registry.ENTITY_TYPE, EntityTypeTags.getTagGroup());
    public static final Node ITEMS = Node.of("item", Registry.ITEM, ItemTags.getTagGroup());
    public static final Permission MODIFY = (Permission) new Permission("permission").addSimpleNodes(Arrays.asList("trust", "distrust", "size", "permission", "setting", "subzone", "name"));

    public static void register() {
        registerPermission((Permission) new Permission("place").addNodes(BLOCK));
        registerPermission((Permission) new Permission("mine").addNodes(BLOCK));
        registerPermission(MODIFY);
        Node child2 = new Node("def");
        Node child3 = new Node("ghi");
        Node test = new Node("test");
        test.addAll(new Node("abc")
                .addAll(new Node("ABC"), new Node("DEF")), child2, child3);
    }

    public static void registerPermission(Permission node) {
        root.add(node);
    }

}
