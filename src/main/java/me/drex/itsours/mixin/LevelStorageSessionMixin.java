package me.drex.itsours.mixin;

import me.drex.itsours.ItsOurs;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelStorage.Session.class)
public abstract class LevelStorageSessionMixin {

    @Inject(
            method = "backupLevelDataFile(Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/world/SaveProperties;Lnet/minecraft/nbt/NbtCompound;)V",
            at = @At("HEAD")
    )
    public void onSave(DynamicRegistryManager dynamicRegistryManager, SaveProperties saveProperties, NbtCompound compoundTag, CallbackInfo ci) {
        if (ItsOurs.INSTANCE != null) ItsOurs.INSTANCE.save();
    }

}
