package me.drex.itsours.claim.flags.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class OwnerContext implements WeightedContext {

    public static final OwnerContext INSTANCE = new OwnerContext();

    private OwnerContext() {
    }

    @Override
    public long getWeight() {
        return OWNER;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.flag.context.owner");
    }

}
