package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.GridIconTab;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemIconTab extends GridIconTab<ItemIconTab.ItemSearchEntry> {

    private static List<ItemSearchEntry> ITEM_INDEX;
    private String lastQuery = "";
    private List<ItemSearchEntry> lastResults = new ArrayList<>();

    public ItemIconTab(Consumer<String> onSelect, Runnable onClose) {
        super(onSelect, onClose);
        ensureItemIndex();
    }

    @Override
    public Text getTitle() {
        return Text.translatable("screen.radial.editor.icon_picker.items");
    }

    @Override
    protected int getSlotSize() {
        return 20;
    }

    @Override
    protected List<ItemSearchEntry> search(String query) {
        if (query.isEmpty()) {
            lastQuery = "";
            lastResults = ITEM_INDEX;
            return ITEM_INDEX;
        }

        List<ItemSearchEntry> source = !lastQuery.isEmpty() && query.startsWith(lastQuery) ? lastResults : ITEM_INDEX;

        List<ItemSearchEntry> results = new ArrayList<>();

        for (ItemSearchEntry entry : source) {
            if (entry.searchText().contains(query)) {
                results.add(entry);
            }
        }

        lastQuery = query;
        lastResults = results;

        return results;
    }

    @Override
    protected void renderIcon(
            DrawContext graphics, int x, int y, int mouseX, int mouseY, ItemSearchEntry item, boolean hovered) {

        graphics.drawItem(item.stack(), x + 2, y + 2);

        if (hovered) {
            MinecraftClient client = MinecraftClient.getInstance();
            graphics.drawTooltip(
                    client.textRenderer,
                    Screen.getTooltipFromItem(client, item.stack()),
                    item.stack().getTooltipData(),
                    mouseX,
                    mouseY);
        }
    }

    @Override
    protected void selectIcon(ItemSearchEntry item) {
        onSelect.accept(item.id().toString());
        onClose.run();
    }

    @Override
    protected Text getItemNarration(ItemSearchEntry item) {
        return item.stack().getName();
    }

    private static void ensureItemIndex() {
        if (ITEM_INDEX != null) return;

        List<ItemSearchEntry> index = new ArrayList<>(Registries.ITEM.size());

        for (Item item : Registries.ITEM) {
            Identifier id = Registries.ITEM.getId(item);
            ItemStack stack = item.getDefaultStack();
            String name = stack.getName().getString();

            index.add(new ItemSearchEntry(item, stack, id, name, (id + " " + name).toLowerCase()));
        }

        ITEM_INDEX = List.copyOf(index);
    }

    public record ItemSearchEntry(Item item, ItemStack stack, Identifier id, String displayName, String searchText) {}
}
