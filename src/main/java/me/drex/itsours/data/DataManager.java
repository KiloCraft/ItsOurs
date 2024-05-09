package me.drex.itsours.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.flags.holder.FlagData;
import me.drex.itsours.user.PlayerData;
import me.drex.itsours.util.Constants;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static me.drex.itsours.ItsOurs.LOGGER;
import static me.drex.itsours.data.ItsOursSchemas.FIXER;

public class DataManager {

    public static final int CURRENT_DATA_VERSION = 9;
    private static Map<UUID, PlayerData> playerData = new HashMap<>();
    private static FlagData defaultFlags = new FlagData();
    public static final Codec<?> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ClaimList.CODEC.fieldOf("claims").forGetter((ignored) -> ClaimList.getClaims().stream().filter(claim -> claim instanceof Claim).map(claim -> (Claim) claim).toList()),
        FlagData.CODEC.fieldOf("default_flags").forGetter(ignored -> DataManager.defaultSettings()),
        Codec.unboundedMap(Uuids.STRING_CODEC, PlayerData.CODEC).fieldOf("players").forGetter(ignored -> DataManager.playerData())
    ).apply(instance, (claims, defaultFlags, playerData) -> {
        init(claims, defaultFlags, playerData);
        return null;
    }));

    public static final Set<String> unknownFlags = new HashSet<>();

    public static void init(List<Claim> claims, FlagData defaultFlags, Map<UUID, PlayerData> players) {
        ClaimList.load(claims);
        playerData = new HashMap<>(players);
        DataManager.defaultFlags = defaultFlags;
    }

    public static PlayerData getUserData(UUID uuid) {
        return playerData.getOrDefault(uuid, new PlayerData(false, false, false, Constants.DEFAULT_CLAIM_BLOCKS));
    }

    public static PlayerData updateUserData(UUID uuid) {
        return playerData.computeIfAbsent(uuid, ignored -> new PlayerData(false, false, false, Constants.DEFAULT_CLAIM_BLOCKS));
    }

    public static void load(MinecraftServer server) {
        Path data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat");
        if (Files.notExists(data)) {
            LOGGER.debug("Claim data file not found.");
        } else {
            try {
                LOGGER.info("Loading claim data {}kB", Files.size(data) / 1024);
                Dynamic<NbtElement> dynamic = new Dynamic<>(NbtOps.INSTANCE, NbtIo.readCompressed(data, NbtSizeTracker.ofUnlimitedBytes()));
                int dataVersion = dynamic.get("dataVersion").asInt(CURRENT_DATA_VERSION);
                dynamic.remove("dataVersion");
                dynamic = FIXER.update(ItsOursTypeReferences.ROOT, dynamic, dataVersion, CURRENT_DATA_VERSION);
                NbtElement element = dynamic.getValue();
                DataResult<?> dataResult = CODEC.parse(NbtOps.INSTANCE, element);
                dataResult.getOrThrow(error -> new IllegalStateException("Failed to parse claim data: '" + error + "'"));
                if (unknownFlags.isEmpty()) {
                    LOGGER.info("Claim data loaded successfully");
                } else {
                    LOGGER.warn("Claim data loaded successfully, with {} unknown flags:", unknownFlags.size());
                    for (String unknownFlag : unknownFlags) {
                        LOGGER.warn(unknownFlag);
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to load claim data {}", data, e);
            }
        }
    }

    public static void save(MinecraftServer server) {
        DataResult<NbtElement> dataResult = CODEC.encodeStart(NbtOps.INSTANCE, null);
        NbtElement nbtElement = dataResult.resultOrPartial(error -> LOGGER.error("Failed tp encode claim data: '{}'", error)).orElseThrow();
        Path data = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat");
        Path backup = server.getSavePath(WorldSavePath.ROOT).resolve("claims.dat_old");
        if (!(nbtElement instanceof NbtCompound root)) {
            return;
        }
        root.putInt("dataVersion", CURRENT_DATA_VERSION);
        if (Files.exists(data)) {
            try {
                LOGGER.debug("Creating backup of: " + data + " (" + Files.size(data) / 1024 + "kb)");
                Files.move(data, backup, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.error("Failed to create backup of {} at {}", data, backup, e);
            }
        }
        try {
            NbtIo.writeCompressed(root, Files.newOutputStream(data));
        } catch (IOException e) {
            LOGGER.error("Failed to save claim data {}", data, e);
        }
    }

    public static FlagData defaultSettings() {
        return defaultFlags;
    }

    public static Map<UUID, PlayerData> playerData() {
        return playerData;
    }
}
