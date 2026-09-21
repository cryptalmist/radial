package dev.velolib.radial;

import com.mojang.blaze3d.platform.InputConstants;
import dev.velolib.radial.api.ShortcutRegistry;
import dev.velolib.radial.api.SlotModeRegistry;
import dev.velolib.radial.config.RadialConfig;
import dev.velolib.radial.config.RadialConfigScreen;
import dev.velolib.radial.integration.MalilibIntegration;
import dev.velolib.radial.mixin.KeyMappingAccessor;
import dev.velolib.radial.ui.screen.RadialScreen;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RadialClient {

    public static final String MOD_ID = "radial";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final String KEY_CATEGORY = "key.category.radial.main";

    public static final KeyMapping OPEN_RADIAL = new KeyMapping(
            "key." + MOD_ID + ".open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEY_CATEGORY);
    public static final KeyMapping BACK_KEY = new KeyMapping(
            "key." + MOD_ID + ".back", InputConstants.Type.KEYSYM, InputConstants.UNKNOWN.getValue(), KEY_CATEGORY);
    public static final KeyMapping[] SLOT_KEYS = new KeyMapping[12];

    static {
        for (int i = 0; i < 12; i++) {
            SLOT_KEYS[i] = new KeyMapping(
                    "key." + MOD_ID + ".slot." + (i + 1),
                    InputConstants.Type.KEYSYM,
                    InputConstants.UNKNOWN.getValue(),
                    KEY_CATEGORY);
        }
    }

    private static final Map<KeyMapping, Integer> keyPressQueue = new ConcurrentHashMap<>();
    private static boolean keyLocked = false;

    public static void lockKey() {
        keyLocked = true;
    }

    /**
     * Helper to blacklist our internal keys from the picker and slot modes.
     */
    public static boolean isRadialInternalKey(KeyMapping key) {
        if (key == OPEN_RADIAL || key == BACK_KEY) return true;
        for (KeyMapping slotKey : SLOT_KEYS) {
            if (key == slotKey) return true;
        }
        return false;
    }

    public static void scheduleKeyPress(KeyMapping key) {
        if (key == null) return;
        KeyMappingAccessor accessor = (KeyMappingAccessor) key;
        accessor.setClickCount(accessor.getClickCount() + 1);
        keyPressQueue.put(key, 2);
    }

    public static void devLogger(String message) {
        if (FMLEnvironment.development) {
            LOGGER.info("DEV - [ {} ]", message);
        }
    }

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Initializing Radial Client...");

        // REGISTER CONFIG
        SlotModeRegistry.init();
        ShortcutRegistry.init();
        RadialConfig.load();

        if (ModList.get().isLoaded("mafglib")) {
            MalilibIntegration.init();
        }

        modEventBus.addListener(RadialClient::registerKeys);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> {
            if (ModList.get().isLoaded("yet_another_config_lib_v3")) {
                return RadialConfigScreen.create(parent);
            }
            // Instead of returning null, return the parent so it stays
            // on the current screen instead of doing nothing.
            return parent;
        });

        NeoForge.EVENT_BUS.addListener(RadialClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(RadialClient::onRenderGuiLayer);
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RADIAL);
        event.register(BACK_KEY);
        for (KeyMapping key : SLOT_KEYS) {
            event.register(key);
        }
    }

    private static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.CROSSHAIR.equals(event.getName())
                && Minecraft.getInstance().screen instanceof RadialScreen) {
            event.setCanceled(true);
        }
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();

        if (OPEN_RADIAL.isDown()) {
            if (!keyLocked && client.screen == null) {
                RadialScreen.prepareRenderer();
                client.setScreen(new RadialScreen());
            }
        } else {
            keyLocked = false;
        }

        //noinspection StatementWithEmptyBody
        while (OPEN_RADIAL.consumeClick()) {}

        if (!keyPressQueue.isEmpty()) {
            var it = keyPressQueue.entrySet().iterator();

            while (it.hasNext()) {
                var entry = it.next();
                KeyMapping key = entry.getKey();
                int ticksLeft = entry.getValue();

                if (ticksLeft > 0) {
                    key.setDown(true);
                    entry.setValue(ticksLeft - 1);
                } else {
                    key.setDown(false);
                    it.remove();
                }
            }
        }
    }
}
