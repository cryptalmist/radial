package dev.velolib.radial;

import dev.velolib.radial.api.ShortcutRegistry;
import dev.velolib.radial.api.SlotModeRegistry;
import dev.velolib.radial.config.RadialConfig;
import dev.velolib.radial.integration.MalilibIntegration;
import dev.velolib.radial.mixin.KeyMappingAccessor;
import dev.velolib.radial.ui.screen.RadialScreen;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadialClient implements ClientModInitializer {

    public static final String MOD_ID = "radial";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of(MOD_ID, "main"));

    public static final KeyBinding OPEN_RADIAL = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key." + MOD_ID + ".open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY));
    public static final KeyBinding BACK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + MOD_ID + ".back", InputUtil.Type.KEYSYM, InputUtil.UNKNOWN_KEY.getCode(), CATEGORY));
    public static final KeyBinding[] SLOT_KEYS = new KeyBinding[12];

    static {
        for (int i = 0; i < 12; i++) {
            SLOT_KEYS[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key." + MOD_ID + ".slot." + (i + 1),
                    InputUtil.Type.KEYSYM,
                    InputUtil.UNKNOWN_KEY.getCode(),
                    CATEGORY));
        }
    }

    private static final Map<KeyBinding, Integer> keyPressQueue = new ConcurrentHashMap<>();
    private static boolean keyLocked = false;

    public static void lockKey() {
        keyLocked = true;
    }

    /**
     * Helper to blacklist our internal keys from the picker and slot modes.
     */
    public static boolean isRadialInternalKey(KeyBinding key) {
        if (key == OPEN_RADIAL || key == BACK_KEY) return true;
        for (KeyBinding slotKey : SLOT_KEYS) {
            if (key == slotKey) return true;
        }
        return false;
    }

    public static void scheduleKeyPress(KeyBinding key) {
        if (key == null) return;
        KeyMappingAccessor accessor = (KeyMappingAccessor) key;
        accessor.setTimesPressed(accessor.getTimesPressed() + 1);
        keyPressQueue.put(key, 2);
    }

    public static void devLogger(String message) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            LOGGER.info("DEV - [ {} ]", message);
        }
    }

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initializing Radial Client...");

        // REGISTER CONFIG
        SlotModeRegistry.init();
        ShortcutRegistry.init();
        RadialConfig.load();

        if (FabricLoader.getInstance().isModLoaded("malilib")) {
            MalilibIntegration.init();
        }

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_RADIAL.isPressed()) {
                if (!keyLocked && client.currentScreen == null) {
                    RadialScreen.prepareRenderer();
                    client.setScreen(new RadialScreen());
                }
            } else {
                keyLocked = false;
            }

            //noinspection StatementWithEmptyBody
            while (OPEN_RADIAL.wasPressed()) {}

            if (!keyPressQueue.isEmpty()) {
                var it = keyPressQueue.entrySet().iterator();

                while (it.hasNext()) {
                    var entry = it.next();
                    KeyBinding key = entry.getKey();
                    int ticksLeft = entry.getValue();

                    if (ticksLeft > 0) {
                        key.setPressed(true);
                        entry.setValue(ticksLeft - 1);
                    } else {
                        key.setPressed(false);
                        it.remove();
                    }
                }
            }
        });
    }
}
