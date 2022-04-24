package me.drex.itsours;

import net.fabricmc.api.DedicatedServerModInitializer;

public class ItsOursMod implements DedicatedServerModInitializer {
    /**
     * Runs the mod initializer on the server environment.
     */
    @Override
    public void onInitializeServer() {
        ItsOurs.INSTANCE.registerEvents();
    }
}
