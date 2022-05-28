/*
package me.drex.itsours.gui.rework;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import me.drex.itsours.claim.permission.rework.PermissionStorage;
import me.drex.itsours.claim.permission.rework.node.Node;
import me.drex.itsours.claim.permission.rework.node.RootNode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class PermissionStorageGui extends PagedGui {

    private final RootNode rootNode;
    private final PermissionStorage storage;

    public PermissionStorageGui(RootNode rootNode, PermissionStorage storage, ServerPlayerEntity player, Node... nodes) {
        super(player);
        this.rootNode = rootNode;
        this.storage = storage;
    }

    @Override
    protected int getPageAmount() {
        return MathHelper.ceil(rootNode.getNodes().size() / (double) PAGE_SIZE);
    }

    @Override
    protected DisplayElement getElement(int id) {
        if (this.rootNode.getNodes().size() > id) {
            Node node = this.rootNode.getNodes().get(id);

            var builder =  new GuiElementBuilder(node.getIcon().asItem())
                    .setName(Text.literal(node.getName()))
                    .setLore(List.of(node.getDescription()))
                    .hideFlags()
                    */
/*.setCallback((x, y, z) -> this.handleSelection(x, y, z, hash))*//*
;

            return DisplayElement.of(builder);
        }

        return DisplayElement.empty();
    }
}
*/
