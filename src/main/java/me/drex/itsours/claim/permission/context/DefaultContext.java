package me.drex.itsours.claim.permission.context;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

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
        return Text.translatable("text.itsours.permission.context.default").styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.default.hover")))
        );
    }

}
