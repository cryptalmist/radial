package dev.velolib.radial.api;

import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public record ShortcutEntry(Text name, Consumer<Screen> openAction) {}
