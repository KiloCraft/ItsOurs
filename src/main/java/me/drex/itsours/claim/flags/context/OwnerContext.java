package me.drex.itsours.claim.flags.context;

import net.minecraft.network.chat.Component;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class OwnerContext implements WeightedContext {

    public static final OwnerContext INSTANCE = new OwnerContext();

    private OwnerContext() {
    }

    @Override
    public long getWeight() {
        return OWNER;
    }

    @Override
    public Component toText() {
        return localized("text.itsours.flag.context.owner");
    }

}
