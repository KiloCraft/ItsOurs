package me.drex.itsours.claim.permission.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class PersonalContext implements WeightedContext {

    public static final PersonalContext INSTANCE = new PersonalContext();

    private PersonalContext() {
    }

    @Override
    public long getWeight() {
        return PERSONAL;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.permission.context.personal");
    }

}
