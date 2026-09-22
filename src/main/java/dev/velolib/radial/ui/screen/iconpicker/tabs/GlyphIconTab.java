package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.GridIconTab;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class GlyphIconTab extends GridIconTab<String> {

    public GlyphIconTab(Consumer<String> onSelect, Runnable onClose) {
        super(onSelect, onClose);
    }

    @Override
    public Text getTitle() {
        return Text.translatable("screen.radial.editor.icon_picker.glyphs");
    }

    @Override
    protected int getSlotSize() {
        return 20;
    }

    @Override
    protected List<String> search(String query) {
        return dev.velolib.radial.util.GlyphCache.getGlyphs().stream()
                .filter(glyph -> glyph.toLowerCase().contains(query))
                .toList();
    }

    @Override
    protected void renderIcon(
            DrawContext graphics, int x, int y, int mouseX, int mouseY, String glyph, boolean hovered) {
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(glyph);
        int textX = x + (getSlotSize() - textWidth) / 2;
        int textY = y + (getSlotSize() - client.textRenderer.fontHeight) / 2;

        graphics.drawText(client.textRenderer, glyph, textX, textY, 0xFFFFFFFF, true);
    }

    @Override
    protected void selectIcon(String glyph) {
        onSelect.accept("radial:glyph." + glyph);
        onClose.run();
    }

    @Override
    protected Text getItemNarration(String glyph) {
        return Text.literal(glyph);
    }
}
