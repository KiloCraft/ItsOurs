package me.drex.itsours.util;

import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;

public class Components {

    public static Text toText(Vec3i vec3i) {
        return Text.translatable("text.itsours.format.vec3i", vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static Text toText(ClaimBox box) {
        return Text.translatable("text.itsours.format.box", toText(box.getMin()), toText(box.getMax()));
    }
}
