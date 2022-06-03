package me.drex.itsours.claim.permission.visitor;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.Permission;
import me.drex.itsours.claim.permission.context.WeightedContext;
import me.drex.itsours.claim.permission.util.Value;

public interface PermissionVisitor {

    void visit(AbstractClaim claim, Permission permission, WeightedContext context, Value value);

    Value getResult();

}
