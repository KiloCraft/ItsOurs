package me.drex.itsours.gui.claims;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.gui.ClaimGui;
import me.drex.itsours.gui.GuiContext;
import me.drex.itsours.gui.PageGui;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.MenuType;

public abstract class ClaimListGui<T extends AbstractClaim> extends PageGui<T> {
    private final boolean advanced;

    public ClaimListGui(GuiContext context, boolean advanced) {
        super(context, MenuType.GENERIC_9x3);
        this.advanced = advanced;
    }

    @Override
    protected GuiElementBuilder guiElement(AbstractClaim claim) {
        boolean currentClaim = claim.contains(context.player.blockPosition()) && claim.getDimension().equals(context.player.level().dimension());
        return guiElement(currentClaim ? Items.GOLD_BLOCK : Items.GRASS_BLOCK, "claimlist.entry", claim.placeholders(context.server()))
            .setCallback(clickType -> {
                if (clickType.isLeft) {
                    switchUi(new ClaimGui(context, claim, advanced));
                } else if (clickType.isRight) {
                    if (!claim.getSubzones().isEmpty()) {
                        switchUi(new SubzoneListGui(context, claim, advanced));
                    } else {
                        fail();
                    }
                }
            });
    }
}
