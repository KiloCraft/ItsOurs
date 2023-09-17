package me.drex.itsours.claim.permission.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class IgnoreContext implements WeightedContext {

    public static final IgnoreContext INSTANCE = new IgnoreContext();

    private IgnoreContext() {
    }

    @Override
    public long getWeight() {
        return IGNORE;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.permission.context.ignore");
    }
}
