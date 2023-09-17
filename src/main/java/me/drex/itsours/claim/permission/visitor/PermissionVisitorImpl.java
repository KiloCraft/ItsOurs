package me.drex.itsours.claim.permission.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;

import java.util.LinkedList;
import java.util.List;

public class PermissionVisitorImpl implements PermissionVisitor {

    private final List<Entry> entries = new LinkedList<>();
    private Value result = Value.UNSET;
    private boolean computed = false;

    protected PermissionVisitorImpl() {
    }

    @Override
    public void visit(AbstractClaim claim, Permission permission, WeightedContext context, Value value) {
        entries.add(new Entry(claim, permission, context, value));
        computed = false;
    }

    @Override
    public void remove(AbstractClaim claim, Permission permission, WeightedContext context) {
        entries.removeIf(entry -> entry.claim().equals(claim) && entry.permission().equals(permission) && entry.context().equals(context));
    }

    private void computeResult() {
        entries.sort(Entry::compareTo);
        Value result = Value.UNSET;
        for (Entry entry : entries) {
            if (entry.value() != Value.UNSET) {
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
