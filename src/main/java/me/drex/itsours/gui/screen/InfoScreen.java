package me.drex.itsours.gui.screen;

import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.util.node.util.Node;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.ClaimContext;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class InfoScreen extends BackScreen<ClaimContext> {

    @Override
    protected String getTitle() {
        return "Claim Panel (" + context.getClaim().getName() + ")";
    }

    public InfoScreen(ServerPlayerEntity player, ClaimContext context) {
        this(player, context, null);
    }
    public InfoScreen(ServerPlayerEntity player, ClaimContext context, SimpleScreen<?> previous) {
        super(player, 3, context, previous);
        SlotEntry<ClaimContext> settings = new SlotEntry<>(Items.COMPARATOR, "Settings", (claimContext, leftClick, shiftClick) -> {
            player.closeHandledScreen();
            SettingScreen settingScreen = new SettingScreen(player, 6, context, this, PermissionList.both, Node.CompareMode.ALPHABET_DESC, AbstractMapScreen.FilterMode.ALL);
            settingScreen.render();
        });
        addSlot(settings, 4);
        SlotEntry<ClaimContext> trusted = new SlotEntry<>(Items.PLAYER_HEAD,"Player Manager", (claimContext, leftClick, shiftClick) -> {
            player.closeHandledScreen();
            TrustedScreen trustedScreen = new TrustedScreen(player, 6, context, this);
            trustedScreen.render();
        });
        addSlot(trusted, 8);
        SlotEntry<ClaimContext> subzones = new SlotEntry<>(Items.SPRUCE_DOOR,"Subzones", (claimContext, leftClick, shiftClick) -> {
            player.closeHandledScreen();
            ListScreen listScreen = new ListScreen(player, claimContext.getClaim(), this);
            listScreen.render();
        });
        addSlot(subzones, 26);
    }

}
