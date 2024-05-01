package me.drex.itsours.claim.flags.context;

import net.minecraft.text.Text;

import static me.drex.message.api.LocalizedMessage.localized;

public class PlayerContext implements WeightedContext {

    public static final PlayerContext INSTANCE = new PlayerContext();

    private PlayerContext() {
    }

    @Override
    public long getWeight() {
        return PLAYER;
    }

    @Override
    public Text toText() {
        return localized("text.itsours.flag.context.player");
    }

}
