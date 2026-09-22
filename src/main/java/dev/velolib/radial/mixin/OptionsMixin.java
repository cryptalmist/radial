package dev.velolib.radial.mixin;

import dev.velolib.radial.config.RadialConfig;
import dev.velolib.radial.ui.screen.RadialScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public class OptionsMixin {

    @Inject(method = "getMenuBackgroundBlurrinessValue", at = @At("HEAD"), cancellable = true)
    private void animateRadialBlur(CallbackInfoReturnable<Integer> cir) {
        MinecraftClient mc = MinecraftClient.getInstance();

        // Check if our screen is the one currently open
        if (mc.currentScreen instanceof RadialScreen radialScreen) {

            // Get the user's actual saved setting (usually 0-10).
            // Use getMenuBackgroundBlurriness().getValue() to avoid recursing into this mixin.
            GameOptions options = (GameOptions) (Object) this;
            int maxBlur = options.getMenuBackgroundBlurriness().getValue();

            // Calculate the animation frame
            float progress = radialScreen.getGlobalRevealProgress(RadialConfig.INSTANCE);
            float easedProgress = radialScreen.easeOutQuint(progress);

            // Override what the game sees for the blur radius!
            cir.setReturnValue((int) (maxBlur * easedProgress));
        }
    }
}
