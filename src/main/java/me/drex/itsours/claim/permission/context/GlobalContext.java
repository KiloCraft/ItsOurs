package me.drex.itsours.claim.permission.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class GlobalContext implements WeightedContext {

    public static final GlobalContext INSTANCE = new GlobalContext();

    private GlobalContext() {
    }

    @Override
    public long getWeight() {
        return GLOBAL;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.permission.context.global");

    }

}
