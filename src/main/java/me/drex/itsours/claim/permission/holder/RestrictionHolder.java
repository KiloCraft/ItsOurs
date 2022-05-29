package me.drex.itsours.claim.permission.holder;

import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RestrictionHolder {

    private final List<UUID> banned = new ArrayList<>();
    private final List<ItemStack> bank = new ArrayList<>();
    private final Object2LongMap<UUID> playerRestrictionBypass = new Object2LongArrayMap<>();
    private ItemStack price = new ItemStack(Items.DIAMOND, 1);
    private long bypassTime = 86400 * 1000;
    private boolean enabled = false;

    public RestrictionHolder(NbtCompound tag) {
        fromNBT(tag);
    }

    public boolean canEnter(UUID uuid) {
        if (banned.contains(uuid)) return true;
        if (playerRestrictionBypass.containsKey(uuid) && enabled) {
            long until = playerRestrictionBypass.getLong(uuid);
            long now = new Date().getTime();
            return now + bypassTime > until;
        }
        return false;
    }

    public List<UUID> getBanned() {
        return banned;
    }

    public List<ItemStack> getBank() {
        return bank;
    }

    public Object2LongMap<UUID> getPlayerRestrictionBypass() {
        return playerRestrictionBypass;
    }

    public ItemStack getPrice() {
        return price;
    }

    public void setPrice(ItemStack price) {
        this.price = price;
    }

    public long getBypassTime() {
        return bypassTime;
    }

    public void setBypassTime(int bypassTime) {
        this.bypassTime = bypassTime;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void fromNBT(NbtCompound tag) {
        this.enabled = tag.getBoolean("enabled");
        if (tag.contains("banned")) {
            NbtList banned = tag.getList("banned", 8);
            for (NbtElement element : banned) {
                this.banned.add(UUID.fromString(element.asString()));
            }
        }
        if (tag.contains("restrictionBypass")) {
            NbtCompound restrictionBypass = tag.getCompound("restrictionBypass");
            for (String key : restrictionBypass.getKeys()) {
                playerRestrictionBypass.put(UUID.fromString(key), restrictionBypass.getLong(key));
            }
        }
        this.price = ItemStack.fromNbt(tag.getCompound("price"));
        this.bypassTime = tag.getInt("byPassTime");
        if (tag.contains("bank")) {
            NbtList bank = tag.getList("bank", 10);
            for (NbtElement nbtElement : bank) {
                this.bank.add(ItemStack.fromNbt((NbtCompound) nbtElement));
            }
        }
    }

    public NbtCompound toNBT() {
        NbtCompound tag = new NbtCompound();
        tag.putBoolean("enabled", enabled);
        if (this.banned.size() > 0) {
            NbtList banned = new NbtList();
            for (UUID uuid : this.banned) {
                banned.add(NbtString.of(uuid.toString()));
            }
            tag.put("banned", banned);
        }
        if (playerRestrictionBypass.object2LongEntrySet().size() > 0) {
            NbtCompound restrictionBypass = new NbtCompound();
            for (Object2LongMap.Entry<UUID> entry : playerRestrictionBypass.object2LongEntrySet()) {
                restrictionBypass.putLong(entry.getKey().toString(), entry.getLongValue());
            }
            tag.put("restrictionBypass", restrictionBypass);
        }
        tag.put("price", price.writeNbt(new NbtCompound()));
        tag.putLong("byPassTime", bypassTime);
        if (this.bank.size() > 0) {
            NbtList bank = new NbtList();
            for (ItemStack itemStack : this.bank) {
                bank.add(itemStack.writeNbt(new NbtCompound()));
            }
            tag.put("bank", bank);
        }
        return tag;
    }
}
