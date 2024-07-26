package me.drex.itsours.claim.flags.node.builder;

import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.Validate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static me.drex.message.api.LocalizedMessage.localized;

public class GroupNodeBuilder extends AbstractNodeBuilder {

    private final List<ChildNode> contained = new LinkedList<>();

    public GroupNodeBuilder(String id) {
        super(id);
    }

    public <T> GroupNodeBuilder(Registry<T> registry, TagKey<T> tagKey, Predicate<T> entryPredicate) {
        super(toShortIdentifier(tagKey.id()));
        ItemConvertible icon = null;
        for (RegistryEntry<T> entry : registry.iterateEntries(tagKey)) {
            if (!entryPredicate.test(entry.value())) continue;
            if (icon == null) {
                icon = getItem(entry.value());;
            }
            final Identifier identifier = registry.getId(entry.value());
            Validate.notNull(identifier, "%s does not contain entry %s", registry.toString(), entry.value().toString());
            LiteralNodeBuilder builder = new LiteralNodeBuilder(registry, entry.value());
            contained(builder.build());
        }
        if (icon != null) {
            icon(icon);
        }
        description(localized("text.itsours.gui.node.group.description", Map.of("count", Text.literal(String.valueOf(this.contained.size())))));
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
