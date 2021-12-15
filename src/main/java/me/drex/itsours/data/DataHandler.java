package me.drex.itsours.data;

import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.permission.roles.RoleManager;
import me.drex.itsours.user.PlayerList;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class DataHandler {

    //DataVersion
    public int dataVersion = 0;
    private final int currentVersion = 1;
    //Data
    private ClaimList claimList;
    private RoleManager roleManager;
    public ClaimList getClaimList() {
        return claimList;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public void load(NbtCompound nbtCompound, boolean firstLoad) {
        dataVersion = nbtCompound.contains("dataVersion") ? nbtCompound.getInt("dataVersion") : 0;
        //load data
        roleManager = new RoleManager(nbtCompound.getCompound("roles"));
        claimList = new ClaimList((NbtList) nbtCompound.get("claims"));
        PlayerList.fromNBT(nbtCompound.getCompound("players"));

        if (dataVersion != currentVersion && !firstLoad) {
            ItsOursMod.LOGGER.info("Updating data from version " + dataVersion + " -> " + currentVersion);
            load(save(), false);
        }
    }

    public NbtCompound save() {
        NbtCompound root = new NbtCompound();
        root.putInt("dataVersion", currentVersion);
        root.put("claims", claimList.toNBT());
        root.put("roles", roleManager.toNBT());
        root.put("players", PlayerList.toNBT());
        return root;
    }

}
