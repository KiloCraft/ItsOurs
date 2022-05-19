package me.drex.itsours.claim.permission.rework;

import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.permission.rework.context.WeightedContext;

public interface PermissionVisitor {

    void visit(AbstractClaim claim, PermissionInterface permission, WeightedContext context, Value value);

    Value getResult();

}
