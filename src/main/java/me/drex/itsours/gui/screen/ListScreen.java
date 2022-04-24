package me.drex.itsours.gui.screen;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.gui.util.ScreenHelper;
import me.drex.itsours.gui.util.SlotEntry;
import me.drex.itsours.gui.util.context.NoContext;
import me.drex.itsours.util.WorldUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListScreen extends PagedScreen<NoContext> {

    private final String title;

    public ListScreen(ServerPlayerEntity player, AbstractClaim claim, SimpleScreen<?> previous) {
        super(player, 4, new NoContext() {
        }, previous);
        title = claim.getFullName() + "'s subzones";
        addClaims(claim.getSubzones());
    }

    public void addClaims(List<? extends AbstractClaim> list) {
        for (AbstractClaim claim : list) {
            ServerWorld world = claim.getWorld();
            Item item;
            if (WorldUtil.isOverworld(world)) {
                item = Items.GRASS_BLOCK;
            } else if (WorldUtil.isNether(world)) {
                item = Items.NETHERRACK;
            } else if (WorldUtil.isEnd(world)) {
                item = Items.END_STONE;
            } else {
                item = Items.STONE;
            }
            ItemStack itemStack = new ItemStack(item);
            ScreenHelper.addLore(itemStack, "Click for more!");
            ScreenHelper.setCustomName(itemStack, claim.getName());
            SlotEntry<NoContext> slotEntry = new SlotEntry<>(itemStack, (noC, leftClick, shiftClick) -> {
                close();
                InfoScreen infoScreen = new InfoScreen(player, () -> claim, this);
                infoScreen.render();
            });
            addPageEntry(slotEntry);
        }
    }

    public ListScreen(ServerPlayerEntity player, UUID uuid) {
        super(player, 4, new NoContext() {});
        title = ScreenHelper.toName(uuid) + "'s claims";
        addClaims(ClaimList.INSTANCE.getClaimsFrom(uuid).stream().filter(abstractClaim -> abstractClaim instanceof Claim).collect(Collectors.toList()));
    }

    @Override
    protected String getTitle() {
        return title;
    }


}
