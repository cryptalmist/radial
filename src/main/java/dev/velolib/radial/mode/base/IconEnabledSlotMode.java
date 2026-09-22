package dev.velolib.radial.mode.base;

import dev.velolib.radial.api.RadialSlot;
import dev.velolib.radial.api.SlotMode;
import dev.velolib.radial.ui.screen.SlotEditorScreen;
import dev.velolib.radial.ui.screen.iconpicker.IconPickerScreen;
import dev.velolib.radial.util.EncoderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public abstract class IconEnabledSlotMode implements SlotMode {

    /**
     * Helper to build the 3 icon widgets (Text Field, Browse Button, Hand Button)
     * using modern layout managers.
     */
    protected void buildIconRow(
            SlotEditorScreen screen, RadialSlot slot, int width, DirectionalLayoutWidget container) {
        int HORIZ_GAP = 5;
        int ICON_BTN_WIDTH = 55;
        int ROW_HEIGHT = 20;
        int iconFieldWidth = width - (ICON_BTN_WIDTH * 2) - (HORIZ_GAP * 2);

        // Group the label and the row of inputs together
        DirectionalLayoutWidget iconGroup = DirectionalLayoutWidget.vertical().spacing(2);

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        // 1. Label
        TextWidget label = new TextWidget(Text.translatable("screen.radial.editor.icon"), textRenderer);
        iconGroup.add(label);

        // 2. Horizontal row for Field + Buttons
        DirectionalLayoutWidget inputRow = DirectionalLayoutWidget.horizontal().spacing(HORIZ_GAP);

        // Icon EditBox
        TextFieldWidget iconField = new TextFieldWidget(
                textRenderer, 0, 0, iconFieldWidth, ROW_HEIGHT, Text.translatable("screen.radial.editor.icon"));
        iconField.setMaxLength(Integer.MAX_VALUE);
        iconField.setText(slot.itemId != null ? slot.itemId : "minecraft:stone");
        iconField.setChangedListener(v -> {
            slot.itemId = v;
            slot.clearCache();
        });
        inputRow.add(iconField);

        // Browse Button
        ButtonWidget browseIconButton = ButtonWidget.builder(
                        Text.translatable("screen.radial.editor.browse"),
                        unused -> MinecraftClient.getInstance().setScreen(new IconPickerScreen(screen, id -> {
                            iconField.setText(id);
                            slot.itemId = id;
                            slot.clearCache();
                        })))
                .dimensions(0, 0, ICON_BTN_WIDTH, ROW_HEIGHT)
                .build();
        inputRow.add(browseIconButton);

        // Hand Button
        ButtonWidget handButton = ButtonWidget.builder(Text.translatable("screen.radial.editor.hand"), unused -> {
                    if (MinecraftClient.getInstance().player != null) {
                        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();
                        String id = !stack.isEmpty()
                                ? EncoderUtils.toGiveCommandString(
                                        stack,
                                        MinecraftClient.getInstance().world.getRegistryManager())
                                : "minecraft:air";
                        iconField.setText(id);
                        slot.itemId = id;
                        slot.clearCache();
                    }
                })
                .dimensions(0, 0, ICON_BTN_WIDTH, ROW_HEIGHT)
                .build();
        inputRow.add(handButton);

        // Add the horizontal row into the vertical group, then add the group to the main container
        iconGroup.add(inputRow);
        container.add(iconGroup);
    }
}
