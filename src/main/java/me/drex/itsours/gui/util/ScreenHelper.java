package me.drex.itsours.gui.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScreenHelper {

    public static void addLore(ItemStack item, Component text) {
        NbtCompound itemTag = item.getNbt();

        if (!item.hasNbt()) {
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
        item.setNbt(itemTag);
    }

    public static void addLore(ItemStack itemStack, String... strings) {
        for (String string : strings) {
            addLore(itemStack, Component.text(string).color(NamedTextColor.WHITE));
        }
    }

    public static void addGlint(ItemStack item) {
        NbtCompound itemTag = item.getNbt();

        if (!item.hasNbt()) {
            itemTag = new NbtCompound();
        }

        if (!itemTag.contains("Enchantments")) {
            itemTag.put("Enchantments", new NbtList());
        }

        NbtList enchantments = itemTag.getList("Enchantments", 10);
        enchantments.add(new NbtCompound());
        itemTag.put("Enchantments", enchantments);
        item.setNbt(itemTag);
    }

    public static void setCustomName(ItemStack item, Component text) {
        item.setCustomName(TextComponentUtil.from(text));
    }

    public static void setCustomName(ItemStack item, String text) {
        setCustomName(item, Component.text(text).color(NamedTextColor.GRAY));
    }

    public static GameProfile getProfile(UUID uuid) {
        Optional<GameProfile> optional = ItsOursMod.server.getUserCache().getByUuid(uuid);
        if (optional.isEmpty()) return new GameProfile(uuid, "???");
        return optional.get();
    }

    public static String toName(UUID uuid) {
        Optional<GameProfile> optional = ItsOursMod.server.getUserCache().getByUuid(uuid);
        String text;
        if (optional.isPresent() && optional.get().isComplete()) {
            text = optional.get().getName();
        } else {
            text = uuid.toString();
        }
        return text;
    }


    public static ItemStack createPlayerHead(String value, UUID uuid) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);

        NbtCompound ownerTag = stack.getOrCreateSubNbt("SkullOwner");
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
        NbtCompound tag = stack.getNbt();
        if (tag==null) tag = new NbtCompound();
        tag.putString("SkullOwner", toName(uuid));
        stack.setNbt(tag);
        return stack;
    }

    public static void insertPlayerHeadAsync(MinecraftServer server, UUID uuid, Consumer<ItemStack> consumer) {
        CompletableFuture.runAsync(() -> {
            ItemStack itemStack = new ItemStack(Items.PLAYER_HEAD);

            Optional<GameProfile> optional = server.getUserCache().getByUuid(uuid);
            MinecraftSessionService sessionService = server.getSessionService();

            if (optional.isEmpty()) {
                itemStack.removeSubNbt("SkullOwner");
                return;
            }
            GameProfile profile = optional.get();

            profile = sessionService.fillProfileProperties(profile, true);
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = sessionService.getTextures(profile, true);

            if (textures.isEmpty()) {
                itemStack.removeSubNbt("SkullOwner");
                return;
            }

            MinecraftProfileTexture texture = textures.get(MinecraftProfileTexture.Type.SKIN);

            NbtCompound ownerTag = itemStack.getOrCreateSubNbt("SkullOwner");
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

}
