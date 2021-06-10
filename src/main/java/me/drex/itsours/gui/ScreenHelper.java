package me.drex.itsours.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScreenHelper {

    public static void addLore(ItemStack item, Component text) {
        NbtCompound itemTag = item.getTag();

        if (!item.hasTag()) {
            itemTag = new NbtCompound();
        }

        if (!itemTag.contains("display")) {
            itemTag.put("display", new NbtCompound());
        }

        NbtList lore = itemTag.getCompound("display").getList("Lore", 8);

        if (lore == null) {
            lore = new NbtList();
        }

        lore.add(NbtString.of(Text.Serializer.toJson(TextComponentUtil.from(text))));
        itemTag.getCompound("display").put("Lore", lore);
        item.setTag(itemTag);
    }

    public static void addGlint(ItemStack item) {
        NbtCompound itemTag = item.getTag();

        if (!item.hasTag()) {
            itemTag = new NbtCompound();
        }

        if (!itemTag.contains("Enchantments")) {
            itemTag.put("Enchantments", new NbtList());
        }

        NbtList enchantments = itemTag.getList("Enchantments", 10);
        enchantments.add(new NbtCompound());
        itemTag.put("Enchantments", enchantments);
        item.setTag(itemTag);
    }

    public static String toName(UUID uuid) {
        GameProfile owner = ItsOursMod.server.getUserCache().getByUuid(uuid);
        String text;
        if (owner != null && owner.isComplete()) {
            text = owner.getName();
        } else {
            text = uuid.toString();
        }
        return text;
    }


    public static ItemStack createPlayerHead(String value, UUID uuid) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound ownerTag = stack.getOrCreateSubTag("SkullOwner");
        ownerTag.putUuid("Id", uuid);

        NbtCompound propertiesTag = new NbtCompound();
        NbtList texturesTag = new NbtList();
        NbtCompound textureValue = new NbtCompound();

        textureValue.putString("Value", value);

        texturesTag.add(textureValue);
        propertiesTag.put("textures", texturesTag);
        ownerTag.put("Properties", propertiesTag);

        return stack;
    }

    public static ItemStack createPlayerHead(UUID uuid) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        NbtCompound tag = stack.getTag();
        if (tag==null) tag = new NbtCompound();
        tag.putString("SkullOwner", toName(uuid));
        stack.setTag(tag);
        return stack;
    }

    public static void insertPlayerHeadAsync(MinecraftServer server, UUID uuid, Consumer<ItemStack> consumer) {
        CompletableFuture.runAsync(() -> {
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);

            GameProfile profile = server.getUserCache().getByUuid(uuid);
            MinecraftSessionService sessionService = server.getSessionService();

            if (profile == null) {
                itemStack.removeSubTag("SkullOwner");
                return;
            }

            profile = sessionService.fillProfileProperties(profile, true);
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = sessionService.getTextures(profile, true);

            if (textures.isEmpty()) {
                itemStack.removeSubTag("SkullOwner");
                return;
            }

            MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);

            NbtCompound ownerTag = itemStack.getOrCreateSubTag("SkullOwner");
            ownerTag.putUuid("Id", profile.getId());
            ownerTag.putString("Name", profile.getName());

            NbtCompound propertiesTag = new NbtCompound();
            NbtList texturesTag = new NbtList();
            NbtCompound textureValue = new NbtCompound();

            textureValue.putString("Value", new String(Base64.encodeBase64(String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", texture.getUrl()).getBytes()), StandardCharsets.UTF_8));

            texturesTag.add(textureValue);
            propertiesTag.put("textures", texturesTag);
            ownerTag.put("Properties", propertiesTag);

            consumer.accept(itemStack);


        });
    }

    public static void openPrevious(ServerPlayerEntity player, GUIScreenHandler screenHandler) {
        GUIScreenHandler previous = screenHandler.previous;
        player.closeHandledScreen();
        if (previous instanceof SettingInfoScreenHandler) {
            player.getServer().execute(() -> SettingInfoScreenHandler.openMenu(player, ((SettingInfoScreenHandler) previous).map, previous.previous, ((SettingInfoScreenHandler) previous).page, ((SettingInfoScreenHandler) previous).node));
        } else if (previous instanceof PermissionInfoScreenHandler) {
            player.getServer().execute(() -> PermissionInfoScreenHandler.openMenu(player, ((PermissionInfoScreenHandler) previous).map, previous.previous, ((PermissionInfoScreenHandler) previous).page, ((PermissionInfoScreenHandler) previous).uuid, ((PermissionInfoScreenHandler) previous).node));
        } else if (previous instanceof ClaimInfoScreenHandler) {
            player.getServer().execute(() -> ClaimInfoScreenHandler.openMenu(player, ((ClaimInfoScreenHandler) previous).claim));
        } else if (previous instanceof TrustedScreenHandler) {
            player.getServer().execute(() -> TrustedScreenHandler.openMenu(player, ((TrustedScreenHandler) previous).claim, previous.previous, ((TrustedScreenHandler) previous).page));
        }
    }

}
