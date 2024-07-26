package me.drex.itsours.gui.flags;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.context.GlobalContext;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import me.drex.message.api.LocalizedMessage;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;

import java.util.Arrays;
import java.util.Collection;

import static me.drex.message.api.LocalizedMessage.localized;

public class SimpleFlagsGui extends PageGui<Flag> {

    public static final Collection<Flag> SIMPLE_FLAGS = Arrays.asList(
        Flag.flag(Flags.PLACE),
        Flag.flag(Flags.MINE),
        Flag.flag(Flags.DAMAGE_ENTITY),
        Flag.flag(Flags.GLIDE),
        Flag.flag(Flags.PVP),
        Flag.flag(Flags.EXPLOSIONS),
        Flag.flag(Flags.PISTON_CROSSES_BORDERS),
        Flag.flag(Flags.FLUID_CROSSES_BORDERS),
        Flag.flag(Flags.SCULK_CROSSES_BORDERS),

        Flag.flag(Flags.USE_ITEM, Node.item(Items.BOW)),
        Flag.flag(Flags.USE_ITEM, Node.item(Items.CROSSBOW)),
        Flag.flag(Flags.USE_ITEM, Node.item(Items.ENDER_PEARL)),

        Flag.flag(Flags.INTERACT_ENTITY, Node.entity(EntityType.BOAT)),
        Flag.flag(Flags.INTERACT_ENTITY, Node.entity(EntityType.MINECART)),
        Flag.flag(Flags.INTERACT_ENTITY, Node.entity(EntityType.VILLAGER)),

        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.CHEST)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.BARREL)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.HOPPER)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.STONECUTTER)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.CARTOGRAPHY_TABLE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.SMITHING_TABLE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.GRINDSTONE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.FURNACE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.BLAST_FURNACE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.SMOKER)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.BEACON)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.ENCHANTING_TABLE)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.JUKEBOX)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.ENDER_CHEST)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.block(Blocks.LECTERN)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.BEDS)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.ALL_SIGNS)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.ANVIL)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.SHULKER_BOXES)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.PRESSURE_PLATES)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.BUTTONS)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.FENCE_GATES)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.TRAPDOORS)),
        Flag.flag(Flags.INTERACT_BLOCK, Node.group(Registries.BLOCK, BlockTags.DOORS))
    );
    private final AbstractClaim claim;

    public SimpleFlagsGui(GuiContext context, AbstractClaim claim) {
        super(context, ScreenHandlerType.GENERIC_9X6);
        this.claim = claim;
        this.setTitle(localized("text.itsours.gui.flags.simple.title", claim.placeholders(context.server())));
    }

    @Override
    public Collection<Flag> elements() {
        ServerCommandSource source = player.getCommandSource();
        return SIMPLE_FLAGS.stream().filter(flag -> {
            boolean allowed = claim.getFlags().get(flag).value;
            return flag.canChange(new Node.ChangeContext(claim, GlobalContext.INSTANCE, allowed ? Value.DEFAULT : Value.ALLOW, source));
        }).toList();
    }

    @Override
    protected GuiElementBuilder guiElement(Flag flag) {

        Value value = claim.getFlags().get(flag);
        if (value == Value.DEFAULT) {
            // Deny is simpler to understand for the average user
            value = Value.DENY;
        }
        boolean allowed = value.value;

        MutableText lore = LocalizedMessage.builder("text.itsours.gui.flags.simple.element.lore")
            .addPlaceholder("description", localized("text.itsours.gui.flags.simple.%s.description".formatted(flag.asString())))
            .addPlaceholder("value", value.format())
            .build();

        ChildNode lastChildNode = flag.getLastChildNode();
        return new GuiElementBuilder(lastChildNode.getIcon().asItem())
            .setName(localized("text.itsours.gui.flags.simple.%s.name".formatted(flag.asString())))
            .addLoreLine(lore)
            .hideDefaultTooltip()
            .glow(allowed)
            .setCallback(() -> {
                Value newValue = allowed ? Value.DEFAULT : Value.ALLOW;
                claim.getFlags().set(flag, newValue);
                click();
                build();
            })
        ;
    }
}
