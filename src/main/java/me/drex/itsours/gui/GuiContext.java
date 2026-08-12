package me.drex.itsours.gui;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Stack;

public class GuiContext {

    public final ServerPlayer player;
    public final Stack<ContextSensitiveGui> guiStack = new Stack<>();

    public GuiContext(ServerPlayer player) {
        this.player = player;
    }

    public MinecraftServer server() {
        return player.level().getServer();
    }
}
