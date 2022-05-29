package me.drex.itsours.claim.permission.context;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class OwnerContext implements WeightedContext {

    public static final OwnerContext INSTANCE = new OwnerContext();

    private OwnerContext() {}

    @Override
    public long getWeight() {
        return OWNER;
    }

    @Override
    public Text toText() {
        return Text.translatable("text.itsours.permission.context.owner").styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.owner.hover")))
        );
    }

}
