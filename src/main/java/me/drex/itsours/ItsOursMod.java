package me.drex.itsours;

import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.claim.util.BlockManager;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ItsOursMod implements DedicatedServerModInitializer {

    public static MinecraftServer server;
    public static Logger LOGGER = LogManager.getLogger();
    public static ItsOursMod INSTANCE;
    private ClaimList claimList;
    private RoleManager roleManager;
    private BlockManager blockManager;

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onStart);
    }

    public void onStart(MinecraftServer server) {
        INSTANCE = this;
        ItsOursMod.server = server;
        load();
    }

    public static String getDirectory(){
        return System.getProperty("user.dir");
    }

    public void load() {
        File data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat").toFile();
        File data_backup = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat_old").toFile();
        if (!data.exists()) {
            LOGGER.info("Data file not found.");
            this.claimList = new ClaimList(new ListTag());
            this.roleManager = new RoleManager(new CompoundTag());
            this.blockManager = new BlockManager(new CompoundTag());
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

    public void save() {
        CompoundTag root = new CompoundTag();
        root.put("claims", claimList.toNBT());
        root.put("roles", roleManager.toNBT());
        root.put("blocks", blockManager.toNBT());
        File data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat").toFile();
        File data_backup = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat_old").toFile();
        //Backup old file
        if (data.exists()) {
            LOGGER.info("Creating backup of: " + data.getName() + " (" + data.length() / 1024 + "kb)");
            if (data_backup.exists()) data_backup.delete();
            data.renameTo(data_backup);
        }
        try {
            data.createNewFile();
            NbtIo.writeCompressed(root, new FileOutputStream(data));
        } catch (IOException e) {
            LOGGER.error("Could not save " + data.getName(), e);
        }

    }
    public RoleManager getRoleManager() {
        return this.roleManager;
    }

    public BlockManager getBlockManager() {
        return this.blockManager;
    }

    public ClaimList getClaimList() {
        return this.claimList;
    }
}
