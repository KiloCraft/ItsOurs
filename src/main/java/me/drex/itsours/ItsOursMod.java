package me.drex.itsours;

import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.command.Manager;
import me.drex.itsours.data.DataHandler;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
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
    private final DataHandler dataHandler = new DataHandler();

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onStart);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            Manager.register(dispatcher);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> load());
    }

    public void onStart(MinecraftServer server) {
        INSTANCE = this;
        ItsOursMod.server = server;
    }

    public void load() {
        PermissionList.register();
        File data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat").toFile();
        File data_backup = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat_old").toFile();
        if (!data.exists()) {
            LOGGER.info("Data file not found.");
            this.dataHandler.load(new NbtCompound(), true);
        } else {
            NbtCompound tag;
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
            this.dataHandler.load(tag, false);
        }
    }

    public static boolean hasPermission(ServerCommandSource src, String permission) {
        return Permissions.check(src, permission, 2);
    }

    public void save() {
        NbtCompound root = dataHandler.save();
        File data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat").toFile();
        File data_backup = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat_old").toFile();
        // Backup old file
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
        return this.dataHandler.getRoleManager();
    }

    public ClaimList getClaimList() {
        return this.dataHandler.getClaimList();
    }

    public int getDataVersion() {
        return dataHandler.dataVersion;
    }

}
