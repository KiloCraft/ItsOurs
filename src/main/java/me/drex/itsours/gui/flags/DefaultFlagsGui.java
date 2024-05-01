package me.drex.itsours.gui.flags;

import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.util.Value;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.gui.GuiContext;
import net.minecraft.text.Text;

import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;

public class DefaultFlagsGui extends AbstractFlagsGui {

    public DefaultFlagsGui(GuiContext context, Flag flag) {
        super(context, DataManager.defaultSettings(), flag);
        this.setTitle(localized("text.itsours.gui.default.title", Map.of("flag", Text.literal(flag.asString()))));
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
