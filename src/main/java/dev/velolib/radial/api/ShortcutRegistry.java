package dev.velolib.radial.api;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.option.*;
import net.minecraft.client.gui.screen.option.ControlsOptionsScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ShortcutRegistry {
    private static final Map<Identifier, ShortcutEntry> REGISTRY = new LinkedHashMap<>();
    private static boolean initialized = false;

    public static void register(Identifier id, ShortcutEntry shortcutEntry) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate registration for shortcut ID: " + id);
        }
        REGISTRY.put(id, shortcutEntry);
    }

    public static Map<Identifier, ShortcutEntry> getRegisteredShortcuts() {
        return java.util.Collections.unmodifiableMap(REGISTRY);
    }

    public static void init() {
        if (initialized) return;
        initialized = true;

        // Main Options
        register(Identifier.of("radial", "options"), new ShortcutEntry(Text.translatable("menu.options"), (parent) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new OptionsScreen(parent, client.options));
        }));

        // Video Settings
        register(
                Identifier.of("radial", "video_settings"),
                new ShortcutEntry(Text.translatable("options.videoTitle"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new VideoOptionsScreen(parent, client, client.options));
                }));

        // Audio Settings
        register(
                Identifier.of("radial", "sound_options"),
                new ShortcutEntry(Text.translatable("options.sounds.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new SoundOptionsScreen(parent, client.options));
                }));

        // Chat Settings
        register(
                Identifier.of("radial", "chat_options"),
                new ShortcutEntry(Text.translatable("options.chat.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new ChatOptionsScreen(parent, client.options));
                }));

        // Accessibility Settings
        register(
                Identifier.of("radial", "accessibility_options"),
                new ShortcutEntry(Text.translatable("options.accessibility.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new AccessibilityOptionsScreen(parent, client.options));
                }));

        // Skin Customization
        register(
                Identifier.of("radial", "skin_customization"),
                new ShortcutEntry(Text.translatable("options.skinCustomisation.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new SkinOptionsScreen(parent, client.options));
                }));

        // Main Controls Screen
        register(
                Identifier.of("radial", "controls"),
                new ShortcutEntry(Text.translatable("options.controls"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new ControlsOptionsScreen(parent, client.options));
                }));

        // Keybinds specific screen
        register(
                Identifier.of("radial", "keybinds"),
                new ShortcutEntry(Text.translatable("controls.keybinds.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new KeybindsScreen(parent, client.options));
                }));

        // Mouse Settings
        register(
                Identifier.of("radial", "mouse_settings"),
                new ShortcutEntry(Text.translatable("options.mouse_settings.title"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new MouseOptionsScreen(parent, client.options));
                }));

        // Language Select (Requires LanguageManager in constructor)
        register(
                Identifier.of("radial", "language"),
                new ShortcutEntry(Text.translatable("options.language"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new LanguageOptionsScreen(parent, client.options, client.getLanguageManager()));
                }));

        // Online Options
        register(
                Identifier.of("radial", "online_options"),
                new ShortcutEntry(Text.translatable("options.online"), (parent) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.setScreen(new OnlineOptionsScreen(parent, client.options));
                }));

        // Other Mods
        FabricLoader.getInstance()
                .getEntrypointContainers("radial", RadialApiEntrypoint.class)
                .forEach(container -> container.getEntrypoint().registerShortcuts());
    }
}
