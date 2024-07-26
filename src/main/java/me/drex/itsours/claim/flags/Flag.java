package me.drex.itsours.claim.flags;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.node.RootNode;
import me.drex.itsours.claim.flags.util.InvalidFlagException;
import me.drex.itsours.data.DataManager;

public interface Flag {

    Codec<Flag> CODEC = Codec.STRING.comapFlatMap(s -> {
        try {
            return DataResult.success(valueOf(s));
        } catch (InvalidFlagException e) {
            // Fail soft
            DataManager.unknownFlags.add(s);
            return DataResult.success(new UnknownFlag());
        }
    }, Flag::asString);

    static Flag valueOf(RootNode rootNode, String input) throws InvalidFlagException {
        return new FlagImpl(rootNode, input);
    }

    static Flag valueOf(String input) throws InvalidFlagException {
        return valueOf(Flags.GLOBAL, input);
    }

    static Flag flag(RootNode rootNode, ChildNode... childNodes) {
        return new FlagImpl(rootNode, childNodes);
    }

    static Flag flag(ChildNode... childNodes) {
        return flag(Flags.GLOBAL, childNodes);
    }

    boolean includes(Flag other);

    void validateContext(Node.ChangeContext context) throws CommandSyntaxException;

    boolean canChange(Node.ChangeContext context);

    Flag withNode(ChildNode node) throws InvalidFlagException;

    ChildNode[] getChildNodes();

    Node getLastNode();

    ChildNode getLastChildNode();

    String asString();

}
