package me.drex.itsours.claim.permission.context;

import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PersonalContext implements WeightedContext {

    private final UUID uuid;

    public PersonalContext(@NotNull UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public long getWeight() {
        return PERMISSION;
    }

    @Override
    public Text toText() {
        return Text.translatable("text.itsours.permission.context.personal").styled(
                style -> style
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("text.itsours.permission.context.personal.hover")))
        );
    }

}
