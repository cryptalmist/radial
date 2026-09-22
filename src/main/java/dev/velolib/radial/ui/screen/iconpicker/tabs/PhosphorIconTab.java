package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.GridIconTab;
import dev.velolib.radial.util.PhosphorIconCache;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PhosphorIconTab extends GridIconTab<PhosphorIconCache.PhosphorIcon> {

    private static final Identifier PHOSPHOR_FONT = Identifier.of("radial", "phosphor");

    public PhosphorIconTab(Consumer<String> onSelect, Runnable onClose) {
        super(onSelect, onClose);
    }

    @Override
    public Text getTitle() {
        return Text.translatable("screen.radial.editor.icon_picker.phosphor");
    }

    @Override
    protected int getSlotSize() {
        return 20;
    }

    @Override
    protected List<PhosphorIconCache.PhosphorIcon> search(String query) {
        return PhosphorIconCache.getIcons().stream()
                .filter(icon -> query.isEmpty() || icon.searchText().contains(query))
                .toList();
    }

    @Override
    protected void renderIcon(
            DrawContext graphics,
            int x,
            int y,
            int mouseX,
            int mouseY,
            PhosphorIconCache.PhosphorIcon icon,
            boolean hovered) {
        MinecraftClient client = MinecraftClient.getInstance();
        Text component = Text.literal(icon.character())
                .setStyle(Style.EMPTY.withFont(new StyleSpriteSource.Font(PHOSPHOR_FONT)));

        int textWidth = client.textRenderer.getWidth(component);
        int textX = x + (getSlotSize() - textWidth) / 2;
        int textY = y + (getSlotSize() - client.textRenderer.fontHeight) / 2 + 5;

        graphics.drawText(client.textRenderer, component, textX, textY, 0xFFFFFFFF, false);

        if (hovered) {
            graphics.drawTooltip(client.textRenderer, Text.literal(icon.name()), mouseX, mouseY);
        }
    }

    @Override
    protected void selectIcon(PhosphorIconCache.PhosphorIcon icon) {
        onSelect.accept("radial:icon." + icon.name());
        onClose.run();
    }

    @Override
    protected Text getItemNarration(PhosphorIconCache.PhosphorIcon icon) {
        return Text.literal(icon.name());
    }
}
