package me.drex.itsours.claim.groups;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.flags.holder.FlagData;
import net.minecraft.util.Uuids;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Group {

    public static final Codec<Group> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        FlagData.CODEC.optionalFieldOf("flags", new FlagData()).forGetter(Group::flags),
        Uuids.INT_STREAM_CODEC.listOf().xmap(Set::copyOf, List::copyOf).optionalFieldOf("players", new HashSet<>()).forGetter(Group::players)
    ).apply(instance, Group::new));

    private final FlagData flags;
    private final Set<UUID> players;

    public Group(FlagData flags, Set<UUID> players) {
        this.flags = flags;
        this.players = new HashSet<>(players);
    }

    public Group() {
        this(new FlagData(), new HashSet<>());
    }

    public FlagData flags() {
        return flags;
    }

    public Set<UUID> players() {
        return players;
    }
}
