package dev.velolib.radial.mixin;

import dev.velolib.radial.ui.screen.RadialScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public class KeyMappingMixin {

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void allowRadialMovement(CallbackInfoReturnable<Boolean> cir) {
        Minecraft client = Minecraft.getInstance();

        if (client.screen instanceof RadialScreen) {

            KeyMapping self = (KeyMapping) (Object) this;

            if (self.getCategory().equals("key.categories.movement")) {

                int keyCode = self.getKey().getValue();
                long handle = client.getWindow().getWindow();
                boolean isPhysicallyPressed = GLFW.glfwGetKey(handle, keyCode) == GLFW.GLFW_PRESS;

                cir.setReturnValue(isPhysicallyPressed);
            }
        }
    }
}
