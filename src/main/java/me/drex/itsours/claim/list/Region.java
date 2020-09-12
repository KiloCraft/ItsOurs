package me.drex.itsours.claim.list;

import java.util.ArrayList;
import java.util.List;

public class Region {

    private static List<Region> list = new ArrayList<>();
    private static final int value = 10;
    private final int x;
    private final int z;

    public Region(int x, int z) {
        this.x = x >> value;
        this.z = z >> value;
        list.add(this);
    }

    public static Region get(int x, int z) {
        for (Region region : list) {
            if (region.getX() == x >> value &&
                    region.getZ() == z >> value)
                return region;
        }
        return new Region(x, z);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String toString() {
        return "x: " + x + " z: " + z;
    }

}
