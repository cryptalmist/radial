package dev.velolib.radial.mode;

import dev.velolib.radial.RadialClient;
import dev.velolib.radial.api.RadialSlot;
import dev.velolib.radial.api.SlotActionContext;
import dev.velolib.radial.mode.base.IconEnabledSlotMode;
import dev.velolib.radial.ui.screen.KeybindPickerScreen;
import dev.velolib.radial.ui.screen.SlotEditorScreen;
import java.util.HashMap;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeybindSlotMode extends IconEnabledSlotMode {
    public static final HashMap<KeyBinding, Consumer<MinecraftClient>> SPECIAL_ACTIONS = new HashMap<>();

    static {
        // TODO: add crash debug key

        SPECIAL_ACTIONS.put(new KeyBinding("key.screenshot", GLFW.GLFW_KEY_F2, KeyBinding.Category.MISC), client -> {
            ScreenshotRecorder.saveScreenshot(client.runDirectory, client.getFramebuffer(), message -> client.inGameHud
                    .getChatHud()
                    .addMessage(message));
        });

        SPECIAL_ACTIONS.put(
                new KeyBinding("key.debug.overlay", GLFW.GLFW_KEY_F3, KeyBinding.Category.DEBUG), client -> {
                    client.keyboard.processF3(new KeyInput(GLFW.GLFW_KEY_F3, 0, 0));
                });
    }

    @Override
    public Text getTranslatedName() {
        return Text.translatable("radial.mode.keybind");
    }

    @Override
    public void buildEditorWidgets(
            SlotEditorScreen screen, RadialSlot slot, int width, DirectionalLayoutWidget container) {
        int HORIZ_GAP = 5;
        int BROWSE_BTN_WIDTH = 55;
        int ROW_HEIGHT = 20;
        int valueFieldWidth = width - BROWSE_BTN_WIDTH - HORIZ_GAP;

        // Group the label and row together vertically
        DirectionalLayoutWidget valueGroup = DirectionalLayoutWidget.vertical().spacing(2);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        TextWidget label = new TextWidget(Text.translatable("screen.radial.editor.value"), textRenderer);
        valueGroup.add(label);

        // Horizontal row for the field + picker button
        DirectionalLayoutWidget inputRow = DirectionalLayoutWidget.horizontal().spacing(HORIZ_GAP);

        TextFieldWidget valueField = new TextFieldWidget(
                textRenderer, 0, 0, valueFieldWidth, ROW_HEIGHT, Text.translatable("screen.radial.editor.value"));
        valueField.setMaxLength(Integer.MAX_VALUE);
        valueField.setText(slot.value != null ? slot.value : "");
        valueField.setChangedListener(v -> slot.value = v);
        inputRow.add(valueField);

        ButtonWidget valueBrowseButton = ButtonWidget.builder(
                        Text.translatable("screen.radial.editor.select"),
                        unused -> MinecraftClient.getInstance().setScreen(new KeybindPickerScreen(screen, id -> {
                            valueField.setText(id);
                            slot.value = id;
                        })))
                .dimensions(0, 0, BROWSE_BTN_WIDTH, ROW_HEIGHT)
                .build();
        inputRow.add(valueBrowseButton);

        valueGroup.add(inputRow);
        container.add(valueGroup);

        // Icon Row
        buildIconRow(screen, slot, width, container);
    }

    @Override
    public void performAction(RadialSlot slot, SlotActionContext context) {
        context.closeScreen();
        MinecraftClient client = MinecraftClient.getInstance();

        for (HashMap.Entry<KeyBinding, Consumer<MinecraftClient>> entry : SPECIAL_ACTIONS.entrySet()) {
            if (entry.getKey().getId().equals(slot.value)) {
                entry.getValue().accept(client);
                return;
            }
        }

        for (KeyBinding key : client.options.allKeys) {
            if (key.getId().equals(slot.value)) {
                // SAFETY CHECK: Abort if it's an internal radial key
                if (dev.velolib.radial.RadialClient.isRadialInternalKey(key)) return;

                if (slot.value.startsWith("key.debug")) {
                    InputUtil.Key inputKey = KeyBindingHelper.getBoundKeyOf(key);
                    int keyCode = inputKey.getCode();
                    var dummyEvent = new KeyInput(keyCode, 0, 0);
                    client.keyboard.processF3(dummyEvent);
                }

                RadialClient.scheduleKeyPress(key);
                break;
            }
        }
    }
}
