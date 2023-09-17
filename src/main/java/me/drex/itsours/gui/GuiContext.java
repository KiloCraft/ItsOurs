package me.drex.itsours.gui;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Stack;

public class GuiContext {

    public final ServerPlayerEntity player;
    public final Stack<ContextSensitiveGui> guiStack = new Stack<>();

    public GuiContext(ServerPlayerEntity player) {
        this.player = player;
    }

    public MinecraftServer server() {
        return player.getServer();
    }
}
