package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.GridIconTab;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

public class EffectIconTab extends GridIconTab<MobEffect> {

    public EffectIconTab(Consumer<String> onSelect, Runnable onClose) {
        super(onSelect, onClose);
    }

    @Override
    public Component getTitle() {
        return Component.translatable("screen.radial.editor.icon_picker.effects");
    }

    @Override
    protected int getSlotSize() {
        return 20;
    }

    @Override
    protected List<MobEffect> search(String query) {
        List<MobEffect> results = new ArrayList<>();
        for (MobEffect effect : BuiltInRegistries.MOB_EFFECT) {
            String name = Component.translatable(effect.getDescriptionId())
                    .getString()
                    .toLowerCase();
            ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect);
            String id = key != null ? key.toString().toLowerCase() : "";
            if (name.contains(query) || id.contains(query)) {
                results.add(effect);
            }
        }
        return results;
    }

    @Override
    protected void renderIcon(
            GuiGraphics graphics, int x, int y, int mouseX, int mouseY, MobEffect effect, boolean hovered) {
        ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if (key == null) {
            return;
        }
        ResourceLocation spriteId = ResourceLocation.fromNamespaceAndPath("minecraft", "mob_effect/" + key.getPath());

        graphics.blitSprite(spriteId, x, y, getSlotSize(), getSlotSize());

        if (hovered) {
            graphics.renderTooltip(
                    Minecraft.getInstance().font, Component.translatable(effect.getDescriptionId()), mouseX, mouseY);
        }
    }

    @Override
    protected void selectIcon(MobEffect effect) {
        ResourceLocation key = BuiltInRegistries.MOB_EFFECT.getKey(effect);
        if (key == null) {
            return;
        }
        onSelect.accept("radial:effect." + key);
        onClose.run();
    }

    @Override
    protected Component getItemNarration(MobEffect effect) {
        return Component.translatable(effect.getDescriptionId());
    }
}
