package me.drex.itsours;

import me.drex.itsours.claim.permission.PermissionManager;
import me.drex.itsours.command.CommandManager;
import me.drex.itsours.data.DataHandler;
import me.drex.itsours.listener.PlayerEventListener;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

public class ItsOurs {

    public MinecraftServer server;
    public static final Logger LOGGER = LogManager.getLogger("itsours");
    public static final ItsOurs INSTANCE = new ItsOurs();
    private final DataHandler dataHandler = new DataHandler();

    private ItsOurs() {}

    protected void registerEvents() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> this.server = server);
        CommandRegistrationCallback.EVENT.register(CommandManager.INSTANCE::register);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerReady);
    }

    public void onServerReady(MinecraftServer server) {
        PlayerEventListener.registerPlayerListeners();
        PermissionManager.register();
        load(server);
    }

    public void load(MinecraftServer server) {
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

    public void save() {
        if (server == null) return;
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

    public int getDataVersion() {
        return dataHandler.dataVersion;
    }

    public static boolean hasPermission(ServerCommandSource src, String permission) {
        if (src.getEntity() != null) {
            return Permissions.check(src, "itsours." + permission, 2);
        }
        return true;
    }

}
