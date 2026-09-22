package dev.velolib.radial.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public interface KeyMappingAccessor {
    @Accessor("timesPressed")
    int getTimesPressed();

    @Accessor("timesPressed")
    void setTimesPressed(int timesPressed);
}
