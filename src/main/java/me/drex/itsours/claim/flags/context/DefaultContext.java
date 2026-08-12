package me.drex.itsours.claim.flags.context;

import net.minecraft.network.chat.Component;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class DefaultContext implements WeightedContext {

    public static final DefaultContext INSTANCE = new DefaultContext();

    private DefaultContext() {
    }

    @Override
    public long getWeight() {
        return DEFAULT;
    }

    @Override
    public Component toText() {
        return localized("text.itsours.flag.context.default");

    }

}
