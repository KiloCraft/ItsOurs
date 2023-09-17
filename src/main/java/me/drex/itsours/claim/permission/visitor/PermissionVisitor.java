package me.drex.itsours.claim.permission.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;

import java.util.List;

public interface PermissionVisitor {

    static PermissionVisitor create() {
        return new PermissionVisitorImpl();
    }

    void visit(AbstractClaim claim, Permission permission, WeightedContext context, Value value);

    void remove(AbstractClaim claim, Permission permission, WeightedContext context);

    Value getResult();

    List<Entry> getEntries();

}
