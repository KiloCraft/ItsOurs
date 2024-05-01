package me.drex.itsours.claim.flags.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.util.Value;

import java.util.List;

public interface FlagVisitor {

    static FlagVisitor create() {
        return new FlagVisitorImpl();
    }

    void visit(AbstractClaim claim, Flag flag, WeightedContext context, Value value);

    void remove(AbstractClaim claim, Flag flag, WeightedContext context);

    Value getResult();

    List<Entry> getEntries();

}
