package me.drex.itsours.gui.flags;

import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.network.chat.Component;

import java.util.Map;


public class DefaultFlagsGui extends AbstractFlagsGui {

    public DefaultFlagsGui(GuiContext context, Flag flag) {
        super(context, DataManager.defaultSettings(), flag);
        this.setTitle(message("text.itsours.gui.default.title", Map.of("flag", Component.literal(flag.asString()))));
    }

    @Override
    boolean setValue(Flag flag, Value value) {
        flagData.set(flag, value);
        return true;
    }

    @Override
    AbstractFlagsGui create(Flag flag) {
        return new DefaultFlagsGui(context, flag);
    }
}
