package me.drex.itsours.claim.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Optional;

public final class ClaimMessages {

    public static final Codec<ClaimMessages> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("enter").forGetter(ClaimMessages::enter),
        Codec.STRING.optionalFieldOf("leave").forGetter(ClaimMessages::leave)
    ).apply(instance, ClaimMessages::new));

    private Optional<String> enter;
    private Optional<String> leave;

    public ClaimMessages(Optional<String> enter, Optional<String> leave) {
        this.enter = enter;
        this.leave = leave;
    }

    public ClaimMessages() {
        this(Optional.empty(), Optional.empty());
    }

    public Optional<String> enter() {
        return enter;
    }

    public void setEnter(String enter) {
        this.enter = Optional.ofNullable(enter);
    }

    public Optional<String> leave() {
        return leave;
    }

    public void setLeave(String leave) {
        this.leave = Optional.ofNullable(leave);
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
