package me.drex.itsours.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.PermissionImpl;
import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.command.SettingCommand;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class ClaimInfoGui extends ClaimGui {

    public static final int GUI_SIZE = 9 * 1;
    private final AbstractClaim claim;

    public ClaimInfoGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, AbstractClaim claim) {
        super(ScreenHandlerType.GENERIC_9X1, player, previousGui);
        this.claim = claim;
        this.setTitle(Text.translatable("text.itsours.gui.claimInfo"));
    }

    /**
     * Executes when the screen is opened
     */
    @Override
    public void onOpen() {
        super.onOpen();
        updateDisplay();
    }

    protected void updateDisplay() {
        for (int index = 0; index < GUI_SIZE; index++) {
            this.setSlot(index, getElement(index));
        }
    }

    protected GuiElementInterface getElement(int index) {
        return switch (index) {
            case 0 -> new GuiElementBuilder(Items.COMPARATOR)
                    .setName(Text.translatable("text.itsours.gui.settings"))
                    .addLoreLine(Text.translatable("text.itsours.gui.settings.description").formatted(Formatting.WHITE))
                    .setCallback((id, type, action, gui) -> new PermissionStorageGui(player, this, Text.translatable("text.itsours.gui.settings"), claim.getPermissionHolder().getSettings(),
                            (pair) -> SettingCommand.INSTANCE.executeSet(player.getCommandSource().withSilent(), claim, pair.getLeft(), pair.getRight()),
                            PermissionImpl.withNodes(PermissionManager.COMBINED)).open())
                    .build();
            case 4 -> new GuiElementBuilder(Items.EMERALD)
                    .setName(Text.translatable("text.itsours.gui.roles"))
                    .addLoreLine(Text.translatable("text.itsours.gui.roles.description").formatted(Formatting.WHITE))
                    .setCallback((id, type, action, gui) -> new PlayerManagerGui(player, this, claim).open())
                    .build();
            case 8 -> new GuiElementBuilder(Items.BARRIER)
                    .setName(Text.translatable("gui.back").formatted(Formatting.RED))
                    .hideFlags()
                    .setCallback((x, y, z) -> {
                        playClickSound(this.player);
                        this.close();
                    }).build();
            default -> EMPTY;
        };
    }

}
