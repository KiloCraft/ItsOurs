package me.drex.itsours.claim.permission.context;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

public class GlobalContext implements WeightedContext {

    public static final GlobalContext INSTANCE = new GlobalContext();

    private GlobalContext() {
    }

    @Override
    public long getWeight() {
        return SETTING;
    }

    @Override
    public Text toText() {
        return Text.translatable("text.itsours.permission.context.global").styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.global.hover")))
        );
    }

}
