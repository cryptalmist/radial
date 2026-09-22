package dev.velolib.radial.ui.screen.iconpicker;

import java.util.function.Consumer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.text.Text;

public interface IconTab {
    Text getTitle();

    // Allows the tab to register its own list/widgets to the main screen
    void setup(int width, int height, Consumer<Drawable> addRenderable, Consumer<Element> addWidget);

    // Render custom backgrounds or overlays (like the inventory tab does)
    void render(DrawContext graphics, int mouseX, int mouseY, float delta);

    boolean mouseClicked(Click click, boolean doubled);

    void updateSearch(String query);

    boolean showSearchBar();
}
