package me.drex.itsours.claim.groups;

import com.mojang.serialization.Codec;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.context.GroupContext;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.util.Value;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.ItemTags;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimGroupManager {

    public static final Codec<ClaimGroupManager> CODEC = Codec.unboundedMap(Codec.STRING, Group.CODEC).xmap(ClaimGroupManager::new, ClaimGroupManager::groups);

    public static final String TRUSTED = "trusted";
    public static final String MODERATOR = "moderator";
    public static final FlagData DEFAULT_TRUSTED;
    public static final FlagData DEFAULT_MODERATOR;

    static {
        {
            DEFAULT_TRUSTED = new FlagData();
            List<ChildNode> nodes = Flags.PLAYER.getNodes();
            for (ChildNode node : nodes) {
                if (!node.equals(Flags.MODIFY)) {
                    Flag flag = Flag.flag(node);
                    DEFAULT_TRUSTED.set(flag, Value.ALLOW);
                }
            }
        }
        {
            DEFAULT_MODERATOR = new FlagData();
            List<ChildNode> nodes = Flags.PLAYER.getNodes();
            for (ChildNode node : nodes) {
                Flag flag = Flag.flag(node);
                DEFAULT_MODERATOR.set(flag, Value.ALLOW);
            }
        }
    }

    private final LinkedHashMap<String, Group> groups;
    public final Group trusted;
    public final Group moderator;

    public ClaimGroupManager(Map<String, Group> groups) {
        if (groups instanceof LinkedHashMap<String, Group> linkedHashMap) {
            this.groups = linkedHashMap;
        } else {
            // Ensure group order
            this.groups = new LinkedHashMap<>(groups);
        }
        assert groups.containsKey(TRUSTED);
        trusted = groups.get(TRUSTED);
        assert groups.containsKey(MODERATOR);
        moderator = groups.get(MODERATOR);
    }

    public ClaimGroupManager() {
        this(new LinkedHashMap<>() {{
            put(TRUSTED, new Group(DEFAULT_TRUSTED.copy(), new HashSet<>()));
            put(MODERATOR, new Group(DEFAULT_MODERATOR.copy(), new HashSet<>()));
        }});
    }

    public static Item getGroupIcon(String groupId) {
        Optional<RegistryEntryList.Named<Item>> optional = Registries.ITEM.getOptional(ItemTags.WOOL);
        if (optional.isPresent()) {
            RegistryEntryList.Named<Item> entries = optional.get();
            return entries.get(Math.abs(groupId.hashCode()) % entries.size()).value();
        } else {
            return Items.STONE;
        }
    }

    public Group createGroup(String groupId) {
        return this.groups.put(groupId, new Group());
    }

    public Set<String> getGroupIds() {
        return groups.keySet();
    }

    public boolean deleteGroup(String groupId) {
        if (groupId.equals(TRUSTED) || groupId.equals(MODERATOR)) {
            return false;
        }
        if (groups.containsKey(groupId)) {
            groups.remove(groupId);
            return true;
        }
        return false;
    }

    @Nullable
    public Group getGroup(String groupId) {
        return groups.get(groupId);
    }

    public int getPriority(String groupId) {
        ArrayList<String> groupIds = new ArrayList<>(this.groups.keySet());
        return groupIds.indexOf(groupId);
    }

    public GroupContext context(String groupId, Group group) {
        return new GroupContext(groupId, getPriority(groupId), group);
    }

    public Map<String, Group> groups() {
        return this.groups;
    }

}
