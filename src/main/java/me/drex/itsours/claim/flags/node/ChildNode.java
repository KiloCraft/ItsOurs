package me.drex.itsours.claim.flags.node;

import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.MutableComponent;

public interface ChildNode extends Node {

    ItemLike getIcon();

    MutableComponent getDescription();

    String getId();

    boolean contains(ChildNode other);

}
