package me.drex.itsours.util;

import com.mojang.authlib.GameProfile;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.command.CommandManager;
import me.drex.itsours.command.InfoCommand;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3i;

import java.util.Optional;
import java.util.UUID;

public class Components {

    public static MutableText toText(Vec3i vec3i) {
        return Text.translatable("text.itsours.format.vec3i", vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public static MutableText toText(ClaimBox box) {
        return Text.translatable("text.itsours.format.box", toText(box.getMin()), toText(box.getMax()));
    }

    public static MutableText toText(UUID uuid) {
        Optional<GameProfile> optional = ItsOurs.INSTANCE.server.getUserCache().getByUuid(uuid);
        return toText(optional.orElse(new GameProfile(uuid, null)));
    }

    public static MutableText toText(GameProfile profile) {
        if (profile.getName() != null) {
            return Text.literal(profile.getName());
        }
        if (profile.getId() != null) {
            return Text.literal(profile.getId().toString()).styled(style -> style
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("text.itsours.format.profile.hover")))
                    .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, profile.getId().toString()))
            );
        }
        return Text.translatable("text.itsours.format.profile.unknown");
    }

    public static MutableText toText(AbstractClaim claim) {
        return Text.literal(claim.getName()).styled(style -> style
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.translatable("text.itsours.format.claim.hover")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format("/%s %s %s", CommandManager.LITERAL, InfoCommand.INSTANCE.getLiteral(), claim.getFullName())))
        );
    }
}
