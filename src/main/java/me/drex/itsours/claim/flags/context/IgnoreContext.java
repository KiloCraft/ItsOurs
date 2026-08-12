package me.drex.itsours.claim.flags.context;

import net.minecraft.network.chat.Component;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class IgnoreContext implements WeightedContext {

    public static final IgnoreContext INSTANCE = new IgnoreContext();

    private IgnoreContext() {
    }

    @Override
    public long getWeight() {
        return IGNORE;
    }

    @Override
    public Component toText() {
        return localized("text.itsours.flag.context.ignore");
    }
}
