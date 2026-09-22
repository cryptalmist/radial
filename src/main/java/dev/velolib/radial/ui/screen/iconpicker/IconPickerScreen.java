package dev.velolib.radial.ui.screen.iconpicker;

import dev.velolib.radial.ui.screen.iconpicker.tabs.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class IconPickerScreen extends Screen {
    private final Screen parent;

    private final List<IconTab> tabs = new ArrayList<>();

    private IconTab currentTab;

    public IconPickerScreen(Screen parent, Consumer<String> onSelect) {
        super(Text.literal("Icon Selector"));
        this.parent = parent;

        // Register Tabs
        tabs.add(new ItemIconTab(onSelect, this::close));
        tabs.add(new InventoryIconTab(onSelect, this::close));
        tabs.add(new EffectIconTab(onSelect, this::close));
        tabs.add(new PhosphorIconTab(onSelect, this::close));
        tabs.add(new GlyphIconTab(onSelect, this::close));

        this.currentTab = tabs.getFirst();
    }

    @Override
    protected void init() {
        clearChildren();

        int tabWidth = Math.min(80, width / Math.max(1, tabs.size()));
        int xOffset = (width - tabWidth * tabs.size()) / 2;

        for (IconTab tab : tabs) {
            ButtonWidget button = ButtonWidget.builder(tab.getTitle(), unused -> setTab(tab))
                    .dimensions(xOffset, 10, tabWidth, 20)
                    .build();

            button.active = (tab != currentTab);
            addDrawableChild(button);

            xOffset += tabWidth;
        }

        int listWidth = Math.min(350, (int) (width * 0.9));
        TextFieldWidget searchField = new TextFieldWidget(
                textRenderer,
                width / 2 - listWidth / 2,
                35,
                listWidth,
                20,
                Text.translatable("screen.radial.editor.search"));
        searchField.setChangedListener(query -> {
            if (currentTab != null) currentTab.updateSearch(query);
        });

        addDrawableChild(searchField);
        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), unused -> close())
                .dimensions(width / 2 - 100, height - 28, 200, 20)
                .build());

        currentTab.setup(width, height, renderable -> addDrawableChild((ClickableWidget) renderable), listener -> {
            if (!this.children().contains(listener)) {
                addSelectableChild((ClickableWidget) listener);
            }
        });

        searchField.visible = currentTab.showSearchBar();
        searchField.setText("");
        setInitialFocus(searchField);
    }

    private void setTab(IconTab tab) {
        this.currentTab = tab;
        this.clearAndInit(); // Triggers init() again to cleanly swap widgets
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        graphics.fillGradient(0, 0, width, height, 0xC0101010, 0xD0101010);

        // Let the tab render its background/custom UI
        currentTab.render(graphics, mouseX, mouseY, delta);

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (currentTab.mouseClicked(click, doubled)) {
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
