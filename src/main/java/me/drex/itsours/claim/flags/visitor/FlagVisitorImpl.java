package me.drex.itsours.claim.flags.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flag;
import me.drex.itsours.claim.flags.context.WeightedContext;
import me.drex.itsours.claim.flags.util.Value;

import java.util.LinkedList;
import java.util.List;

public class FlagVisitorImpl implements FlagVisitor {

    private final List<Entry> entries = new LinkedList<>();
    private Value result = Value.DEFAULT;
    private boolean computed = false;

    protected FlagVisitorImpl() {
    }

    @Override
    public void visit(AbstractClaim claim, Flag flag, WeightedContext context, Value value) {
        entries.add(new Entry(claim, flag, context, value));
        computed = false;
    }

    @Override
    public void remove(AbstractClaim claim, Flag flag, WeightedContext context) {
        entries.removeIf(entry -> entry.claim().equals(claim) && entry.flag().equals(flag) && entry.context().equals(context));
    }

    private void computeResult() {
        entries.sort(Entry::compareTo);
        Value result = Value.DEFAULT;
        for (Entry entry : entries) {
            if (entry.value() != Value.DEFAULT) {
                result = entry.value();
            }
        }
        this.result = result;
        computed = true;
    }

    public List<Entry> getEntries() {
        if (!computed) computeResult();
        return entries;
    }

    @Override
    public Value getResult() {
        if (!computed) computeResult();
        return result;
    }

}
