package dev.velolib.radial.ui.screen.iconpicker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public abstract class GridIconTab<T> implements IconTab {
    protected final Consumer<String> onSelect;
    protected final Runnable onClose;

    private IconGridList listWidget;
    private List<T> currentResults = new ArrayList<>();

    public GridIconTab(Consumer<String> onSelect, Runnable onClose) {
        this.onSelect = onSelect;
        this.onClose = onClose;
    }

    protected abstract int getSlotSize();

    protected abstract List<T> search(String query);

    protected abstract void renderIcon(
            GuiGraphics graphics, int x, int y, int mouseX, int mouseY, T item, boolean hovered);

    protected abstract void selectIcon(T item);

    protected abstract Component getItemNarration(T item);

    @Override
    public void setup(int width, int height, Consumer<Renderable> addRenderable, Consumer<GuiEventListener> addWidget) {
        int listWidth = Math.min(350, (int) (width * 0.9));
        int top = 65;
        int bottom = height - 40;
        int left = width / 2 - listWidth / 2;

        listWidget = new IconGridList(Minecraft.getInstance(), listWidth, Math.max(1, bottom - top), top, 24);

        listWidget.updateSizeAndPosition(listWidth, Math.max(1, bottom - top), top);
        listWidget.setX(left);

        addRenderable.accept(listWidget);
        addWidget.accept(listWidget);

        updateSearch("");
    }

    @Override
    public void updateSearch(String query) {
        currentResults = search(query.trim().toLowerCase());
        rebuildRows();
    }

    private void rebuildRows() {
        if (listWidget == null) return;

        int usableWidth = listWidget.getRowWidth();
        int columns = Math.max(1, usableWidth / getSlotSize());

        List<IconGridEntry> rows = new ArrayList<>();

        for (int start = 0; start < currentResults.size(); start += columns) {
            int end = Math.min(start + columns, currentResults.size());

            rows.add(new IconGridEntry(new ArrayList<>(currentResults.subList(start, end))));
        }

        listWidget.setEntries(rows);
        listWidget.setScrollAmount(0.0);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        /* Managed by listWidget */
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false; /* Managed by listWidget */
    }

    @Override
    public boolean showSearchBar() {
        return true;
    }

    private class IconGridList extends ContainerObjectSelectionList<IconGridEntry> {

        public IconGridList(Minecraft mc, int w, int h, int y, int rowHeight) {
            super(mc, w, h, y, rowHeight);
        }

        public void setEntries(Collection<IconGridEntry> entries) {
            replaceEntries(entries);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

        @Override
        public int getRowWidth() {
            int availableWidth = Math.min(330, getWidth() - 20);
            int slotSize = getSlotSize();

            int columns = Math.max(1, availableWidth / slotSize);

            return columns * slotSize;
        }
    }

    private class IconGridEntry extends ContainerObjectSelectionList.Entry<IconGridEntry> {

        private final List<T> items;
        private int lastLeft;
        private int lastTop;

        private IconGridEntry(List<T> items) {
            this.items = items;
        }

        @Override
        public void render(
                GuiGraphics graphics,
                int index,
                int top,
                int left,
                int width,
                int height,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta) {
            this.lastLeft = left;
            this.lastTop = top;
            int slotSize = getSlotSize();

            int verticalOffset = Math.max(0, (24 - slotSize) / 2);

            for (int i = 0; i < items.size(); i++) {
                int x = left + i * slotSize;
                int y = top + verticalOffset;

                boolean slotHovered = mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize;

                if (slotHovered) {
                    graphics.fill(x, y, x + slotSize, y + slotSize, 0x40FFFFFF);
                }

                renderIcon(graphics, x, y, mouseX, mouseY, items.get(i), slotHovered);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) {
                return false;
            }

            int slotSize = getSlotSize();

            int verticalOffset = Math.max(0, (24 - slotSize) / 2);

            for (int i = 0; i < items.size(); i++) {
                int x = lastLeft + i * slotSize;
                int y = lastTop + verticalOffset;

                if (mouseX >= x && mouseX < x + slotSize && mouseY >= y && mouseY < y + slotSize) {

                    selectIcon(items.get(i));
                    return true;
                }
            }

            return false;
        }

        public Component getNarration() {
            return items.isEmpty() ? Component.literal("Empty row") : getItemNarration(items.getFirst());
        }

        public List<? extends NarratableEntry> narratables() {
            return List.of();
        }
    }
}
