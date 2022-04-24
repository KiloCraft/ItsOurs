package me.drex.itsours.data;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.user.PlayerList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class DataHandler {

    // DataVersion
    public int dataVersion = 0;
    private final int currentVersion = 1;
    // Data
    private RoleManager roleManager;

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public void load(NbtCompound nbtCompound, boolean firstLoad) {
        dataVersion = nbtCompound.contains("dataVersion") ? nbtCompound.getInt("dataVersion") : 0;
        // Load data
        roleManager = new RoleManager(nbtCompound.getCompound("roles"));
        ClaimList.INSTANCE.fromNBT(nbtCompound.getList("claims", NbtElement.COMPOUND_TYPE));
        PlayerList.fromNBT(nbtCompound.getCompound("players"));

        if (dataVersion != currentVersion && !firstLoad) {
            ItsOurs.LOGGER.info("Updating data from version " + dataVersion + " -> " + currentVersion);
            load(save(), false);
        }
    }

    public NbtCompound save() {
        NbtCompound root = new NbtCompound();
        root.putInt("dataVersion", currentVersion);
        root.put("claims", ClaimList.INSTANCE.toNBT());
        if (this.roleManager != null) root.put("roles", roleManager.toNBT());
        root.put("players", PlayerList.toNBT());
        return root;
    }

}
