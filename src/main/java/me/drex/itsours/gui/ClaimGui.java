package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class ClaimGui extends SimpleGui {

    private final SimpleGui previousGui;

    public static final GuiElement EMPTY = new GuiElement(ItemStack.EMPTY, GuiElementInterface.EMPTY_CALLBACK);
    public static final GuiElement FILLER = new GuiElementBuilder(Items.WHITE_STAINED_GLASS_PANE)
            .setName(Text.empty())
            .hideFlags().build();

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param type   the screen handler that the client should display
     * @param player the player to server this gui to
     */
    public ClaimGui(ScreenHandlerType<?> type, ServerPlayerEntity player, @Nullable SimpleGui previousGui) {
        super(type, player, false);
        this.previousGui = previousGui;
    }

    protected Collection<GameProfile> asCommandTarget(String name) throws CommandSyntaxException {
        return Collections.singleton(player.server.getUserCache().findByName(name).orElseThrow(GameProfileArgumentType.UNKNOWN_PLAYER_EXCEPTION::create));
    }

    public static void playClickSound(ServerPlayerEntity player) {
        player.playSound(SoundEvents.UI_BUTTON_CLICK, SoundCategory.MASTER, 1, 1);
    }

    public static void playFailSound(ServerPlayerEntity player) {
        player.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1, 1);
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
