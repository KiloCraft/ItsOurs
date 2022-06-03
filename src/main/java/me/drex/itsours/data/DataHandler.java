package me.drex.itsours.data;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.user.PlayerList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class DataHandler {

    // DataVersion
    public int dataVersion;
    private final int currentVersion = 2;
    // Data

    public void load(NbtCompound nbtCompound, boolean firstLoad) {
        dataVersion = nbtCompound.contains("dataVersion") ? nbtCompound.getInt("dataVersion") : 0;
        // Load data
        RoleManager.INSTANCE.load(nbtCompound.get("roles"));
        ClaimList.INSTANCE.load(nbtCompound.getList("claims", NbtElement.COMPOUND_TYPE));
        PlayerList.fromNBT(nbtCompound.getCompound("players"));

        if (dataVersion != currentVersion && !firstLoad) {
            ItsOurs.LOGGER.info("Updating data from version " + dataVersion + " -> " + currentVersion);
            load(save(), false);
        }
    }

    public NbtCompound save() {
        NbtCompound root = new NbtCompound();
        root.putInt("dataVersion", currentVersion);
        root.put("claims", ClaimList.INSTANCE.save());
        root.put("roles", RoleManager.INSTANCE.save());
        root.put("players", PlayerList.toNBT());
        return root;
    }

}
