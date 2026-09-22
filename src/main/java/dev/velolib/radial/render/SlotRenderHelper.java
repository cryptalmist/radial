package dev.velolib.radial.render;

import dev.velolib.radial.api.RadialSlot;
import dev.velolib.radial.util.PhosphorIconCache;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Style;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class SlotRenderHelper {

    private static final Identifier PHOSPHOR_FONT = Identifier.of("radial", "phosphor");

    private static final StyleSpriteSource PHOSPHOR_FONT_SOURCE = new StyleSpriteSource.Font(PHOSPHOR_FONT);

    private SlotRenderHelper() {}

    public static ItemStack resolveDynamicItem(String itemId) {
        MinecraftClient minecraft = MinecraftClient.getInstance();

        if (minecraft.player == null) {
            return null;
        }

        if (itemId != null && itemId.startsWith("radial:slot.")) {

            String[] parts = itemId.split("\\.");

            if (parts.length >= 2) {

                String type = parts[1];

                PlayerInventory inv = minecraft.player.getInventory();

                try {

                    switch (type) {
                        case "hotbar":
                            if (parts.length >= 3) {

                                int hbIndex = Integer.parseInt(parts[2]);

                                if (hbIndex >= 0 && hbIndex < 9) {

                                    return inv.getMainStacks().get(hbIndex);
                                }
                            }

                            break;

                        case "inventory":
                            if (parts.length >= 3) {

                                int invIndex = Integer.parseInt(parts[2]);

                                if (invIndex >= 0 && invIndex < 27) {

                                    return inv.getMainStacks().get(invIndex + 9);
                                }
                            }

                            break;

                        case "armor":
                            if (parts.length >= 3) {

                                String armorSlot = parts[2];

                                switch (armorSlot) {
                                    case "head":
                                        return minecraft.player.getEquippedStack(EquipmentSlot.HEAD);

                                    case "chest":
                                        return minecraft.player.getEquippedStack(EquipmentSlot.CHEST);

                                    case "legs":
                                        return minecraft.player.getEquippedStack(EquipmentSlot.LEGS);

                                    case "feet":
                                        return minecraft.player.getEquippedStack(EquipmentSlot.FEET);
                                }
                            }

                            break;

                        case "offhand":
                            return minecraft.player.getOffHandStack();
                    }

                } catch (NumberFormatException ignored) {
                }
            }

            return ItemStack.EMPTY;
        }

        return null;
    }

    public static ItemStack getDisplayStack(RadialSlot slot) {
        ItemStack dynamic = resolveDynamicItem(slot.itemId);

        if (dynamic != null) {
            return dynamic;
        }

        return slot.getRenderStack();
    }

    public static void renderSlotIcon(DrawContext graphics, RadialSlot slot, float x, float y) {
        if (slot == null || slot.itemId == null || !slot.mode.shouldRenderIcon()) {

            return;
        }

        String itemId = slot.itemId;

        // --- Phosphor Icon ---
        if (itemId.startsWith("radial:icon.")) {

            String iconName = itemId.substring("radial:icon.".length());

            renderPhosphorIcon(graphics, iconName, x, y);

            return;
        }

        // --- Glyph ---
        if (itemId.startsWith("radial:glyph.")) {

            String glyph = itemId.substring("radial:glyph.".length());

            MinecraftClient client = MinecraftClient.getInstance();

            int textWidth = client.textRenderer.getWidth(glyph);

            int centerX = (int) x + Math.round((26 - textWidth) / 2.0f);

            int centerY = (int) y + Math.round((26 - 8) / 2.0f);

            graphics.drawText(client.textRenderer, glyph, centerX, centerY, 0xFFFFFFFF, false);

            return;
        }

        // --- Effect ---
        if (itemId.startsWith("radial:effect.")) {

            String effectKey = itemId.substring("radial:effect.".length());

            Identifier id = Identifier.tryParse(effectKey);

            if (id == null) {
                return;
            }

            Optional<StatusEffect> effect = Registries.STATUS_EFFECT.getOptionalValue(id);

            effect.ifPresent(value -> {
                String path = Objects.requireNonNull(Registries.STATUS_EFFECT.getId(value))
                        .getPath();

                Identifier spriteId = Identifier.of("minecraft", "mob_effect/" + path);

                graphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, spriteId, (int) x + 4, (int) y + 4, 18, 18);
            });

            return;
        }

        // --- Dynamic Slot ---
        ItemStack stack = getDisplayStack(slot);

        if (stack != null && !stack.isEmpty()) {

            graphics.drawItem(stack, (int) x + 5, (int) y + 5);
        }
    }

    private static void renderPhosphorIcon(DrawContext graphics, String iconName, float x, float y) {
        PhosphorIconCache.PhosphorIcon icon = PhosphorIconCache.getIcons().stream()
                .filter(candidate -> candidate.name().equals(iconName))
                .findFirst()
                .orElse(null);

        if (icon == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        String glyph = icon.character();

        Text component = Text.literal(glyph).setStyle(Style.EMPTY.withFont(PHOSPHOR_FONT_SOURCE));

        int textWidth = textRenderer.getWidth(component);

        int textX = (int) x + Math.round((26 - textWidth) / 2.0F);

        int textY = (int) y + Math.round((26 - textRenderer.fontHeight) / 2.0F) + 4;

        graphics.drawText(textRenderer, component, textX, textY, 0xFFFFFFFF, false);
    }
}
