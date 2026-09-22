package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.GridIconTab;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EffectIconTab extends GridIconTab<StatusEffect> {

    public EffectIconTab(Consumer<String> onSelect, Runnable onClose) {
        super(onSelect, onClose);
    }

    @Override
    public Text getTitle() {
        return Text.translatable("screen.radial.editor.icon_picker.effects");
    }

    @Override
    protected int getSlotSize() {
        return 20;
    }

    @Override
    protected List<StatusEffect> search(String query) {
        return Registries.STATUS_EFFECT.stream()
                .filter(effect -> {
                    String name = Text.translatable(effect.getTranslationKey())
                            .getString()
                            .toLowerCase();
                    String id = Objects.requireNonNull(Registries.STATUS_EFFECT.getId(effect))
                            .toString()
                            .toLowerCase();
                    return name.contains(query) || id.contains(query);
                })
                .toList();
    }

    @Override
    protected void renderIcon(
            DrawContext graphics, int x, int y, int mouseX, int mouseY, StatusEffect effect, boolean hovered) {
        String path =
                Objects.requireNonNull(Registries.STATUS_EFFECT.getId(effect)).getPath();
        Identifier spriteId = Identifier.of("minecraft", "mob_effect/" + path);

        graphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, spriteId, x, y, getSlotSize(), getSlotSize());

        if (hovered) {
            MinecraftClient client = MinecraftClient.getInstance();
            graphics.drawTooltip(client.textRenderer, Text.translatable(effect.getTranslationKey()), mouseX, mouseY);
        }
    }

    @Override
    protected void selectIcon(StatusEffect effect) {
        String effectId =
                Objects.requireNonNull(Registries.STATUS_EFFECT.getId(effect)).toString();
        onSelect.accept("radial:effect." + effectId);
        onClose.run();
    }

    @Override
    protected Text getItemNarration(StatusEffect effect) {
        return Text.translatable(effect.getTranslationKey());
    }
}
