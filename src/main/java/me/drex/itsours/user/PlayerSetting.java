package me.drex.itsours.user;

public enum PlayerSetting {
    CACHED_FLIGHT("cached_flight", false),
    DEBUG("debug", false),
    IGNORE("ignore", false),
    BLOCKS("blocks", 500),
    FLIGHT("flight", false);

    protected final String id;
    protected final Object defaultValue;

    PlayerSetting(String id, Object defaultValue) {
        this.id = id;
        this.defaultValue = defaultValue;
    }

}
