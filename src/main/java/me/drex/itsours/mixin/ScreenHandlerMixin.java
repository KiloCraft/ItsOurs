package me.drex.itsours.mixin;

import me.drex.itsours.gui.util.ScreenSync;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin implements ScreenSync {


    @Mutable
    @Shadow @Final public int syncId;

    @Override
    public void setSyncId(int syncId) {
        this.syncId = syncId;
    }
}
