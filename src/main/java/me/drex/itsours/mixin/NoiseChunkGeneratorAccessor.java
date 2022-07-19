package me.drex.itsours.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NoiseChunkGenerator.class)
public interface NoiseChunkGeneratorAccessor {

    @Accessor
    BlockState getDefaultBlock();

}
