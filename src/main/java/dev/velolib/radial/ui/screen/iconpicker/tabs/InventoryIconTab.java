package dev.velolib.radial.ui.screen.iconpicker.tabs;

import dev.velolib.radial.ui.screen.iconpicker.IconTab;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class InventoryIconTab implements IconTab {

    private static final Identifier INVENTORY_TEXTURE =
            Identifier.of("minecraft", "textures/gui/container/inventory.png");
    private static final int INV_WIDTH = 176;
    private static final int INV_HEIGHT = 166;
    private static final int INV_SLOT_SIZE = 18;

    private final Consumer<String> onSelect;
    private final Runnable onClose;
    private int screenWidth, screenHeight;

    public InventoryIconTab(Consumer<String> onSelect, Runnable onClose) {
        this.onSelect = onSelect;
        this.onClose = onClose;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("screen.radial.editor.icon_picker.inventory");
    }

    @Override
    public boolean showSearchBar() {
        return false;
    }

    @Override
    public void updateSearch(String query) {}

    @Override
    public void setup(int width, int height, Consumer<Drawable> addRenderable, Consumer<Element> addWidget) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        PlayerInventory inventory = mc.player.getInventory();
        int bgX = screenWidth / 2 - INV_WIDTH / 2;
        int bgY = screenHeight / 2 - INV_HEIGHT / 2 + 10;

        Text infoText = Text.translatable("screen.radial.editor.icon_picker.inventory.info");
        graphics.drawText(
                mc.textRenderer,
                infoText,
                screenWidth / 2 - mc.textRenderer.getWidth(infoText) / 2,
                bgY - 15,
                0xFFAAAAAA,
                true);
        graphics.drawTexture(
                RenderPipelines.GUI_TEXTURED, INVENTORY_TEXTURE, bgX, bgY, 0.0F, 0.0F, INV_WIDTH, INV_HEIGHT, 256, 256);

        // Hotbar
        for (int i = 0; i < 9; i++) {
            drawInvSlot(
                    graphics,
                    mc,
                    mouseX,
                    mouseY,
                    bgX + 7 + i * 18,
                    bgY + 141,
                    inventory.getMainStacks().get(i));
        }

        // Main Inventory
        for (int i = 0; i < 27; i++) {
            drawInvSlot(
                    graphics,
                    mc,
                    mouseX,
                    mouseY,
                    bgX + 7 + (i % 9) * 18,
                    bgY + 83 + (i / 9) * 18,
                    inventory.getMainStacks().get(i + 9));
        }

        // Armor
        EquipmentSlot[] armorSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < armorSlots.length; i++) {
            drawInvSlot(
                    graphics, mc, mouseX, mouseY, bgX + 7, bgY + 7 + i * 18, mc.player.getEquippedStack(armorSlots[i]));
        }

        // Offhand
        drawInvSlot(graphics, mc, mouseX, mouseY, bgX + 76, bgY + 61, mc.player.getOffHandStack());
    }

    private void drawInvSlot(
            DrawContext graphics, MinecraftClient mc, int mouseX, int mouseY, int x, int y, ItemStack stack) {
        if (!stack.isEmpty()) {
            graphics.drawItem(stack, x + 1, y + 1);
        }
        if (isHovered(mouseX, mouseY, x, y)) {
            graphics.fill(x, y, x + INV_SLOT_SIZE, y + INV_SLOT_SIZE, 0x40FFFFFF);
            if (!stack.isEmpty()) {
                graphics.drawTooltip(
                        mc.textRenderer, Screen.getTooltipFromItem(mc, stack), stack.getTooltipData(), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int bgX = screenWidth / 2 - INV_WIDTH / 2;
        int bgY = screenHeight / 2 - INV_HEIGHT / 2 + 10;

        // Hotbar check
        for (int i = 0; i < 9; i++) {
            if (isHovered(mouseX, mouseY, bgX + 7 + i * 18, bgY + 141)) {
                onSelect.accept("radial:slot.hotbar." + i);
                onClose.run();
                return true;
            }
        }

        // Main inventory check
        for (int i = 0; i < 27; i++) {
            if (isHovered(mouseX, mouseY, bgX + 7 + (i % 9) * 18, bgY + 83 + (i / 9) * 18)) {
                onSelect.accept("radial:slot.inventory." + i);
                onClose.run();
                return true;
            }
        }

        // Armor check
        String[] armorNames = {"head", "chest", "legs", "feet"};
        for (int i = 0; i < armorNames.length; i++) {
            if (isHovered(mouseX, mouseY, bgX + 7, bgY + 7 + i * 18)) {
                onSelect.accept("radial:slot.armor." + armorNames[i]);
                onClose.run();
                return true;
            }
        }

        // Offhand check
        if (isHovered(mouseX, mouseY, bgX + 76, bgY + 61)) {
            onSelect.accept("radial:slot.offhand");
            onClose.run();
            return true;
        }

        return false;
    }

    private boolean isHovered(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + INV_SLOT_SIZE && mouseY >= y && mouseY < y + INV_SLOT_SIZE;
    }
}
