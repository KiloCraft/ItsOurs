package me.drex.itsours.claim.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public final class ClaimMessages {

    public static final Codec<ClaimMessages> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("enter", null).forGetter(ClaimMessages::enter),
        Codec.STRING.optionalFieldOf("leave", null).forGetter(ClaimMessages::leave)
    ).apply(instance, ClaimMessages::new));

    private String enter;
    private String leave;

    public ClaimMessages(String enter, String leave) {
        this.enter = enter;
        this.leave = leave;
    }

    public ClaimMessages() {
        this(null, null);
    }

    public String enter() {
        return enter;
    }

    public void setEnter(String enter) {
        this.enter = enter;
    }

    public String leave() {
        return leave;
    }

    public void setLeave(String leave) {
        this.leave = leave;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ClaimMessages) obj;
        return Objects.equals(this.enter, that.enter) &&
            Objects.equals(this.leave, that.leave);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enter, leave);
    }

}
