package me.drex.itsours.gui.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface CommandCallback<T> {

    int execute(T value) throws CommandSyntaxException;

}
