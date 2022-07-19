package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.mixin.NoiseChunkGeneratorAccessor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractClaimListGui<T extends AbstractClaim> extends PagedGui<T> {

    public AbstractClaimListGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, Text title) {
        super(player, previousGui);
        this.setTitle(title);
    }

    @Override
    protected GuiElement asDisplayElement(T abstractClaim) {
        boolean hasSubzones = !abstractClaim.getSubzones().isEmpty();
        var builder = new GuiElementBuilder(getIcon(abstractClaim))
                .setName(Text.literal(abstractClaim.getName()))
                .setCallback((index, type, action, gui) -> {
                    if (type.isLeft) {
                        if (hasSubzones) {
                            new SubzoneListGui(player, this, abstractClaim).open();
                        } else {
                            playFailSound(player);
                        }
                    } else {
                        new ClaimInfoGui(player, this, abstractClaim).open();
                    }
                });
        if (hasSubzones) builder.addLoreLine(Text.translatable("text.itsours.gui.leftClick", Text.translatable("text.itsours.gui.claimList.element.lore.subzones")).formatted(Formatting.WHITE));
        builder.addLoreLine(Text.translatable("text.itsours.gui.rightClick", Text.translatable("text.itsours.gui.claimList.element.lore.info")).formatted(Formatting.WHITE));
        return builder.build();
    }

    private Item getIcon(T abstractClaim) {
        try {
            ServerWorld world = player.server.getWorld(abstractClaim.getDimension());
            if (world != null) {
                ChunkGenerator chunkGenerator = world.getChunkManager().getChunkGenerator();
                if (chunkGenerator instanceof NoiseChunkGeneratorAccessor accessor) {
                    return accessor.getDefaultBlock().getBlock().asItem();
                }
            }
        } catch (Exception ignored) {
            // If any problems occur, just return the default icon
        }
        return Items.GRASS_BLOCK;
    }

}
