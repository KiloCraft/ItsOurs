package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.AnvilInputGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import me.drex.itsours.gui.util.CommandCallback;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import static me.drex.itsours.gui.ClaimGui.playFailSound;

public class PlayerSelectorGui extends AnvilInputGui {

    private final SimpleGui previousGui;
    private final CommandCallback<String> commandCallback;

    public PlayerSelectorGui(ServerPlayerEntity player, @Nullable SimpleGui previousGui, CommandCallback<String> commandCallback) {
        super(player, false);
        this.previousGui = previousGui;
        this.commandCallback = commandCallback;
        this.setTitle(Text.translatable("text.itsours.gui.playerSelector"));
    }

    /**
     * Executes when the screen is opened
     */
    @Override
    public void onOpen() {
        super.onOpen();
        updateDisplay();
    }

    /**
     * Executes when the input is changed.
     *
     * @param input the new input
     */
    @Override
    public void onInput(String input) {
        super.onInput(input);
        updateDisplay();
    }

    protected void updateDisplay() {
        setSlot(2,
                new GuiElementBuilder(Items.PLAYER_HEAD)
                        .setName(Text.literal(getInput()))
                        // Name must be non-empty, so we default to "DrexHD"
                        .setSkullOwner(new GameProfile(null, StringUtils.isBlank(getInput()) ? "DrexHD" : getInput()), null)
                        .setCallback((index, type1, action, gui) -> {
                            try {
                                if (!StringUtils.isBlank(getInput())) {
                                    commandCallback.execute(getInput());
                                }
                            } catch (CommandSyntaxException exception) {
                                player.getCommandSource().sendError(Texts.toText(exception.getRawMessage()));
                                playFailSound(player);
                            }
                            close();
                        })
                        .hideFlags()

        );
    }

    /**
     * Executes when the screen is closed
     */
    @Override
    public void onClose() {
        if (previousGui != null) {
            previousGui.close(true);
            previousGui.open();
        }
    }
}
