package me.drex.itsours.claim.permission.context;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

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
        return Text.translatable("text.itsours.permission.context.ignore").styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.ignore.hover")))
        );
    }
}
