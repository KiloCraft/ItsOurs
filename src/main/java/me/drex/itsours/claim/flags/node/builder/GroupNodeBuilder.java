package me.drex.itsours.claim.flags.node.builder;

import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.Registry;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.itsours.util.LocalizedMessage.localized;

public class GroupNodeBuilder extends AbstractNodeBuilder {

    private final List<ChildNode> contained = new LinkedList<>();

    public GroupNodeBuilder(String id) {
        super(id);
    }

    public <T> GroupNodeBuilder(Registry<T> registry, TagKey<T> tagKey, Predicate<T> entryPredicate) {
        super(toShortIdentifier(tagKey.location()));
        ItemLike icon = null;
        for (Holder<T> entry : registry.getTagOrEmpty(tagKey)) {
            if (!entryPredicate.test(entry.value())) continue;
            if (icon == null) {
                icon = getItem(entry.value());;
            }
            final Identifier identifier = registry.getKey(entry.value());
            Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.value().toString());
            LiteralNodeBuilder builder = new LiteralNodeBuilder(registry, entry.value());
            contained(builder.build());
        }
        if (icon != null) {
            icon(icon);
        }
        description(localized("text.itsours.gui.node.group.description", Map.of("count", Component.literal(String.valueOf(this.contained.size())))));
    }

    public GroupNodeBuilder contained(ChildNode contained) {
        this.contained.add(contained);
        return this;
    }

    public GroupNodeBuilder contained(List<ChildNode> contained) {
        this.contained.addAll(contained);
        return this;
    }

    @Override
    public GroupNode build() {
        sortChildNodes();
        return new GroupNode(id, description, childNodes, icon, changePredicate, contained);
    }
}
