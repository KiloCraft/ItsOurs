package me.drex.itsours.claim.flags.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class DefaultContext implements WeightedContext {

    public static final DefaultContext INSTANCE = new DefaultContext();

    private DefaultContext() {
    }

    @Override
    public long getWeight() {
        return DEFAULT;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.flag.context.default");

    }

}
