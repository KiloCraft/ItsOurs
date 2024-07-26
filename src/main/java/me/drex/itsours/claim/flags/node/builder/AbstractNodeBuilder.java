package me.drex.itsours.claim.flags.node.builder;

import me.drex.itsours.claim.flags.node.AbstractChildNode;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.Node;
import me.drex.itsours.claim.flags.util.FlagBuilderUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.item.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public abstract class AbstractNodeBuilder {

    protected final String id;
    protected final List<ChildNode> childNodes = new LinkedList<>();
    protected MutableText description = Text.empty();
    protected ItemConvertible icon = Items.STONE;
    protected Predicate<Node.ChangeContext> changePredicate = context -> true;

    public AbstractNodeBuilder(String id) {
        this.id = id;
    }

    public AbstractNodeBuilder description(String translationId) {
        this.description = localized("text.itsours.flags." + translationId + ".description");
        return this;
    }

    public AbstractNodeBuilder description(MutableText description) {
        this.description = description;
        return this;
    }

    public AbstractNodeBuilder icon(ItemConvertible icon) {
        this.icon = icon;
        return this;
    }

    protected <T> ItemConvertible getItem(T entry) {
        Item item = null;
        if (entry instanceof ItemConvertible convertible) {
            Item asItem = convertible.asItem();
            if (!asItem.equals(Items.AIR)) {
                item = asItem;
            }
        } else if (entry instanceof EntityType<?> entityType) {
            ItemStack itemStack = FlagBuilderUtil.ENTITY_PICK_BLOCK_STATE.get(entityType);
            if (itemStack == null || itemStack.isEmpty()) {
                item = Items.EGG;
            } else {
                item = itemStack.getItem();
            }
        }
        return item;
    }

    public AbstractNodeBuilder then(Collection<ChildNode> childNodes) {
        this.childNodes.addAll(childNodes);
        return this;
    }

    public AbstractNodeBuilder then(ChildNode childNode) {
        this.childNodes.add(childNode);
        return this;
    }

    public AbstractNodeBuilder then(AbstractNodeBuilder childNode) {
        this.childNodes.add(childNode.build());
        return this;
    }

    public AbstractNodeBuilder predicate(Predicate<Node.ChangeContext> changePredicate) {
        this.changePredicate = this.changePredicate.and(changePredicate);
        return this;
    }

    protected void sortChildNodes() {
        this.childNodes.sort((o1, o2) -> o1.getId().compareToIgnoreCase(o2.getId()));
    }

    public abstract AbstractChildNode build();

    public static String toShortIdentifier(Identifier identifier) {
        if (identifier.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return identifier.getPath();
        }
        return identifier.toString();
    }

}
