package me.drex.itsours;

import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.claim.util.BlockManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ItsOursMod implements ModInitializer {

    public static MinecraftServer server;
    public static Logger LOGGER = LogManager.getLogger();
    private ClaimList claimList;
    private RoleManager roleManager;
    private BlockManager blockManager;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onStart);
    }

    public void onStart(MinecraftServer server) {
        ItsOursMod.server = server;
        load();
    }

    public static String getDirectory(){
        return System.getProperty("user.dir");
    }

    public void load() {
        File data = new File(getDirectory() + "/world/claims.dat");
        File data_backup = new File(getDirectory() + "/world/claims.dat_old");

        if (!data.exists()) {
            LOGGER.info("Data file not found.");
            this.claimList = new ClaimList(new ListTag());
            this.roleManager = new RoleManager(new CompoundTag());
        } else {
            CompoundTag tag;
            try {
                tag = NbtIo.readCompressed(new FileInputStream(data));
            } catch (IOException e) {
                LOGGER.error("Could not load " + data.getName(), e);
                if (data_backup.exists()) {
                    LOGGER.info("Attempting to load " + data_backup.getName() + "...");
                    try {
                        tag = NbtIo.readCompressed(new FileInputStream(data_backup));
                    } catch (IOException ioException) {
                        throw new RuntimeException("Could not load backup - Crashing server to save data.");
                    }
                } else {
                    throw new RuntimeException("Could not load backup - Crashing server to save data.");
                }
            }
            this.claimList = new ClaimList((ListTag) tag.get("claims"));
            this.roleManager = new RoleManager(tag.getCompound("roles"));
            this.blockManager = new BlockManager(tag.getCompound("blocks"));
        }
    }

}
