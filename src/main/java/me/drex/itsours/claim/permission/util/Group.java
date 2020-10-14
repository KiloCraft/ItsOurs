package me.drex.itsours.claim.permission.util;

import me.drex.itsours.claim.permission.util.node.AbstractNode;
import me.drex.itsours.claim.permission.util.node.GroupNode;
import me.drex.itsours.claim.permission.util.node.SingleNode;
import net.minecraft.block.*;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public final List<AbstractNode> list;

    public Group(List<AbstractNode> list) {
        this.list = list;
    }

    public static final Group BLOCK = new Group(new ArrayList<AbstractNode>() {{
        List<String> trapdoors = new ArrayList<>();
        List<String> doors = new ArrayList<>();
        List<String> buttons = new ArrayList<>();
        List<String> containers = new ArrayList<>();
        List<String> signs = new ArrayList<>();
        List<String> shulkerboxes = new ArrayList<>();
        for (Block block : Registry.BLOCK) {
            String s = Permission.toString(block);
            this.add(new SingleNode(s));
            if (block instanceof TrapdoorBlock) trapdoors.add(s);
            if (block instanceof DoorBlock) doors.add(s);
            if (block instanceof AbstractButtonBlock) buttons.add(s);
            if (isContainer(block)) containers.add(s);
            if (block instanceof AbstractSignBlock) signs.add(s);
            if (isShulkerBox(block)) shulkerboxes.add(s);
        }
        this.add(new GroupNode("TRAPDOORS", trapdoors.toArray(new String[0])));
        this.add(new GroupNode("DOORS", doors.toArray(new String[0])));
        this.add(new GroupNode("BUTTONS", buttons.toArray(new String[0])));
        this.add(new GroupNode("CONTAINERS", containers.toArray(new String[0])));
        this.add(new GroupNode("SIGNS", signs.toArray(new String[0])));
        this.add(new GroupNode("SHULKERBOXES", shulkerboxes.toArray(new String[0])));
    }});

    public static boolean isShulkerBox(Block block) {
        return block instanceof ShulkerBoxBlock;
    }

    public static boolean isContainer(Block block) {
        return block instanceof BlockWithEntity || isShulkerBox(block);
    }
}
