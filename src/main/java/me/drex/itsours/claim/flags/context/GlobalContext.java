package me.drex.itsours.claim.flags.context;

import net.minecraft.network.chat.Component;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class GlobalContext implements WeightedContext {

    public static final GlobalContext INSTANCE = new GlobalContext();

    private GlobalContext() {
    }

    @Override
    public long getWeight() {
        return GLOBAL;
    }

    @Override
    public Component toText() {
        return localized("text.itsours.flag.context.global");

    }

}
