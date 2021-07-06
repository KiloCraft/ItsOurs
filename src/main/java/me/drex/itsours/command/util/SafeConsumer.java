package me.drex.itsours.command.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface SafeConsumer<T> {

    void accept(T t) throws CommandSyntaxException;

}
