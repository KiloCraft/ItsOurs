package me.drex.itsours.user;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.util.Constants;

import java.util.Objects;

public final class PlayerData {

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("ignore", false).forGetter(PlayerData::ignore),
        Codec.BOOL.optionalFieldOf("select", false).forGetter(PlayerData::select),
        Codec.BOOL.optionalFieldOf("flight", false).forGetter(PlayerData::flight),
        Codec.LONG.optionalFieldOf("blocks", Constants.DEFAULT_CLAIM_BLOCKS).forGetter(PlayerData::blocks)
    ).apply(instance, PlayerData::new));

    private boolean ignore;
    private boolean select;
    private boolean flight;
    private long blocks;

    public PlayerData(boolean ignore, boolean select, boolean flight, long blocks) {
        this.ignore = ignore;
        this.select = select;
        this.flight = flight;
        this.blocks = blocks;
    }

    public boolean ignore() {
        return ignore;
    }

    public boolean select() {
        return select;
    }

    public boolean flight() {
        return flight;
    }

    public long blocks() {
        return blocks;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public void setFlight(boolean flight) {
        this.flight = flight;
    }

    public void setBlocks(long blocks) {
        this.blocks = blocks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PlayerData) obj;
        return this.ignore == that.ignore &&
            this.select == that.select &&
            this.flight == that.flight &&
            this.blocks == that.blocks;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignore, select, flight, blocks);
    }

    @Override
    public String toString() {
        return "UserData[" +
            "ignore=" + ignore + ", " +
            "select=" + select + ", " +
            "flight=" + flight + ", " +
            "blocks=" + blocks + ']';
    }


}
