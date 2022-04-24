// TODO:
/*
package me.drex.itsours.command.bulk;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.command.Command;
import me.drex.itsours.command.bulk.util.CriteriumParser;
import me.drex.itsours.command.bulk.util.Range;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.Pair;
import me.drex.itsours.util.TextComponentUtil;
import me.drex.itsours.util.TextPage;
import me.drex.itsours.util.text.SLiteralText;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.stream.Collectors;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SelectCommand extends Command {

    public static Map<UUID, List<AbstractClaim>> selectedClaims = new HashMap<>();
    public static Map<UUID, TextPage> cachedPages = new HashMap<>();

    public static void register(LiteralArgumentBuilder<ServerCommandSource> command) {
        LiteralCommandNode<ServerCommandSource> select = literal("select").executes(ctx -> select(ctx.getSource(), ""))
                .then(argument("criteria", StringArgumentType.greedyString()).suggests(CriteriumParser.getInstance())
                        .executes(ctx -> select(ctx.getSource(), StringArgumentType.getString(ctx, "criteria"))))
                .build();
        command.then(select);
    }

    public static int select(ServerCommandSource source, String criteria) throws CommandSyntaxException {
        List<Pair<String, Object>> propertiesEntries = CriteriumParser.getInstance().rawEntries(criteria);
        List<AbstractClaim> claims = ClaimList.INSTANCE.getClaims().stream().filter(claim -> claim instanceof Claim).collect(Collectors.toList());
        for (Pair<String, Object> pair : propertiesEntries) {
            String key = pair.getKey();
            switch (key) {
                case "x" -> {
                    Range range = new Range((String) pair.getValue());
                    claims = claims.stream().filter(claim -> range.includes(claim.min.getX(), claim.max.getX())).collect(Collectors.toList());
                }
                case "y" -> {
                    Range range = new Range((String) pair.getValue());
                    claims = claims.stream().filter(claim -> range.includes(claim.min.getY(), claim.max.getY())).collect(Collectors.toList());
                }
                case "z" -> {
                    Range range = new Range((String) pair.getValue());
                    claims = claims.stream().filter(claim -> range.includes(claim.min.getZ(), claim.max.getY())).collect(Collectors.toList());
                }
                case "area" -> {
                    Range range = new Range((String) pair.getValue());
                    claims = claims.stream().filter(claim -> range.includes(claim.getArea())).collect(Collectors.toList());
                }
                case "dimension" -> claims = claims.stream().filter(claim -> pair.getValue().equals(claim.getDimension().getValue())).collect(Collectors.toList());
                case "owner" ->
                        //TODO: Implement name support
                        claims = claims.stream().filter(claim -> pair.getValue().equals(claim.getOwner().toString())).collect(Collectors.toList());
            }
        }
        ServerPlayerEntity player = source.getPlayer();
        ClaimPlayer claimPlayer = (ClaimPlayer) player;
        claimPlayer.sendMessage(Component.text("You selected " + claims.size() + " claim" + ((claims.size() == 1) ? "" : "s") + ".").color(Color.LIGHT_GREEN));
        selectedClaims.put(player.getUuid(), claims);
        updatePageCache(player.getUuid());
        return 1;
    }

    public static void updatePageCache(UUID uuid) {
        List<AbstractClaim> claims = selectedClaims.getOrDefault(uuid, new ArrayList<>());
        List<Text> entries = new ArrayList<>();
        for (AbstractClaim claim : claims) {
            MutableText text = SLiteralText.of(claim.getName()).formatted(Formatting.GOLD).append(" (").formatted(Formatting.GRAY)
                    .append(TextComponentUtil.from(TextComponentUtil.toName(claim.getOwner(), NamedTextColor.AQUA)))
                    .append(SLiteralText.of(")"));
            text.styled(style ->
                    style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claim info " + claim.getFullName())));
            entries.add(text);
        }
        cachedPages.put(uuid, new TextPage(SLiteralText.of("Selected Claims").formatted(Formatting.LIGHT_PURPLE), entries, "/claim bulk list %s"));
    }


}
*/
