package me.drex.itsours.claim.permission.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PermissionVisitorImpl implements PermissionVisitor {

    private Value result = Value.UNSET;
    private boolean computed = false;
    private final List<Entry> entries = new LinkedList<>();

    public PermissionVisitorImpl() {}

    @Override
    public void visit(AbstractClaim claim, Permission permission, WeightedContext context, Value value) {
        entries.add(new Entry(claim, permission, context, value));
        computed = false;
    }

    private void computeResult() {
        entries.sort(Entry::compareTo);
        Value result = Value.UNSET;
        for (Entry entry : entries) {
            if (entry.value != Value.UNSET) {
                result = entry.value;
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

    public record Entry(AbstractClaim claim,
                         Permission permission,
                         WeightedContext context,
                         Value value) implements Comparable<Entry> {

        @Override
        public int compareTo(@NotNull PermissionVisitorImpl.Entry other) {
            // Claim depth
            int claimDepthCompare = Integer.compare(claim.getDepth(), other.claim.getDepth());
            if (claimDepthCompare != 0) return claimDepthCompare;
            // Context
            int contextCompare = context.compareTo(other.context);
            if (contextCompare != 0) return contextCompare;
            // Permission
            if (permission.includes(other.permission)) return -1;
            else if (other.permission.includes(permission)) return 1;
            else return 0;
        }

    }

}
