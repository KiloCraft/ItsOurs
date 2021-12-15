package me.drex.itsours.gui.util;

public interface Consumer<T> {

    void accept(T t, boolean leftClick, boolean shiftClick);

}
