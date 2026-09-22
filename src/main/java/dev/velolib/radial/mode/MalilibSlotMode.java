package dev.velolib.radial.mode;

import dev.velolib.radial.api.RadialSlot;
import dev.velolib.radial.api.SlotActionContext;
import dev.velolib.radial.integration.MalilibIntegration;
import dev.velolib.radial.mode.base.IconEnabledSlotMode;
import dev.velolib.radial.ui.screen.MalilibSelectionScreen;
import dev.velolib.radial.ui.screen.SlotEditorScreen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.Text;

public class MalilibSlotMode extends IconEnabledSlotMode {
    @Override
    public Text getTranslatedName() {
        return Text.translatable("radial.mode.malilib");
    }

    @Override
    public boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded("malilib");
    }

    @Override
    public void performAction(RadialSlot slot, SlotActionContext context) {
        context.closeScreen();

        if (FabricLoader.getInstance().isModLoaded("malilib")) {
            MalilibIntegration.executeHotkey(slot.value);
        }
    }

    @Override
    public void buildEditorWidgets(
            SlotEditorScreen screen, RadialSlot slot, int width, DirectionalLayoutWidget container) {
        int HORIZ_GAP = 5;
        int BROWSE_BTN_WIDTH = 55;
        int ROW_HEIGHT = 20;
        int valueFieldWidth = width - BROWSE_BTN_WIDTH - HORIZ_GAP;

        DirectionalLayoutWidget valueGroup = DirectionalLayoutWidget.vertical().spacing(2);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        TextWidget label = new TextWidget(Text.translatable("screen.radial.editor.value"), textRenderer);
        valueGroup.add(label);

        DirectionalLayoutWidget inputRow = DirectionalLayoutWidget.horizontal().spacing(HORIZ_GAP);

        TextFieldWidget valueField = new TextFieldWidget(
                textRenderer, 0, 0, valueFieldWidth, ROW_HEIGHT, Text.translatable("screen.radial.editor.value"));
        valueField.setMaxLength(Integer.MAX_VALUE);
        valueField.setText(slot.value != null ? slot.value : "");
        valueField.setChangedListener(v -> slot.value = v);
        inputRow.add(valueField);

        ButtonWidget valueBrowseButton = ButtonWidget.builder(
                        Text.translatable("screen.radial.editor.select"),
                        unused -> MinecraftClient.getInstance().setScreen(new MalilibSelectionScreen(screen, action -> {
                            valueField.setText(action.id());
                            slot.value = action.id();
                        })))
                .dimensions(0, 0, BROWSE_BTN_WIDTH, ROW_HEIGHT)
                .build();
        inputRow.add(valueBrowseButton);

        valueGroup.add(inputRow);
        container.add(valueGroup);

        buildIconRow(screen, slot, width, container);
    }
}
