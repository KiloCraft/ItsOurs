package me.drex.itsours.claim.flags.context;

import net.minecraft.network.chat.Component;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class PlayerContext implements WeightedContext {

    public static final PlayerContext INSTANCE = new PlayerContext();

    private PlayerContext() {
    }

    @Override
    public long getWeight() {
        return PLAYER;
    }

    @Override
    public Component toText() {
        return localized("text.itsours.flag.context.player");
    }

}
