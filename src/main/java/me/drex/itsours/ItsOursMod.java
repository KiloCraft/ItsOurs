package me.drex.itsours;

import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.permission.PermissionList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.command.Manager;
import me.drex.itsours.user.PlayerList;
import me.drex.itsours.util.PermissionHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class ItsOursMod implements DedicatedServerModInitializer {

    public static MinecraftServer server;
    public static Logger LOGGER = LogManager.getLogger();
    public static ItsOursMod INSTANCE;
    public static final UUID serverUUID = new UUID(0, 0);
    private ClaimList claimList;
    private RoleManager roleManager;
    private PlayerList playerList;
    private PermissionHandler permissionHandler;

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
            this.claimList = new ClaimList(new NbtList());
            this.roleManager = new RoleManager(new NbtCompound());
            this.playerList = new PlayerList(new NbtCompound());
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
            this.roleManager = new RoleManager(tag.getCompound("roles"));
            this.claimList = new ClaimList((NbtList) tag.get("claims"));
            this.playerList = new PlayerList(tag.getCompound("players"));
        }
        this.permissionHandler = new PermissionHandler();
    }

    public void save() {
        NbtCompound root = new NbtCompound();
        root.put("claims", claimList.toNBT());
        root.put("roles", roleManager.toNBT());
        root.put("players", playerList.toNBT());
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

    public ClaimList getClaimList() {
        return this.claimList;
    }

    public PlayerList getPlayerList() {
        return this.playerList;
    }

    public PermissionHandler getPermissionHandler() {
        return this.permissionHandler;
    }

}
