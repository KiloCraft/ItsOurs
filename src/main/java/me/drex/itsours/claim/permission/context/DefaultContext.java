package me.drex.itsours.claim.permission.context;

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
        return localized("text.itsours.permission.context.default");

    }

}
