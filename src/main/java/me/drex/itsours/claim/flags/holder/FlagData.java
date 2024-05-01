package me.drex.itsours.claim.flags.holder;

import com.mojang.serialization.Codec;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.UnknownFlag;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.claim.flags.visitor.FlagVisitor;
import me.drex.itsours.util.PlaceholderUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;

public class FlagData {

    public static final Codec<FlagData> CODEC = Codec.unboundedMap(Flag.CODEC, Codec.BOOL).xmap(FlagData::new, instance -> instance.data);

    private final Map<Flag, Boolean> data;

    private FlagData(Map<Flag, Boolean> data) {
        this.data = new HashMap<>();
        // Ignore unknown flags
        data.forEach((flag, value) -> {
            if (flag instanceof UnknownFlag) return;
            this.data.put(flag, value);
        });
    }

    public FlagData() {
        this(new HashMap<>());
    }

    public void visit(AbstractClaim claim, Flag flag, WeightedContext context, FlagVisitor visitor) {
        for (Map.Entry<Flag, Boolean> entry : data.entrySet()) {
            if (entry.getKey().includes(flag)) {
                visitor.visit(claim, entry.getKey(), context, Value.of(entry.getValue()));
            }
        }
    }

    public void set(Flag flag, Value value) {
        if (value == Value.DEFAULT) {
            data.remove(flag);
        } else {
            data.put(flag, value.value);
        }
    }

    public Value get(Flag flag) {
        Boolean bool = data.get(flag);
        if (bool != null) {
            return Value.of(bool);
        } else {
            return Value.DEFAULT;
        }
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int size() {
        return data.size();
    }

    public Text toText() {
        return PlaceholderUtil.list(data.entrySet(), flagEntry ->
                Map.of("flag", Text.literal(flagEntry.getKey().asString()).formatted(flagEntry.getValue() ? Formatting.GREEN : Formatting.RED)),
            "text.itsours.placeholders.flags");
    }

    public FlagData copy() {
        return new FlagData(new HashMap<>(data));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof FlagData holder) {
            return data.equals(holder.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
