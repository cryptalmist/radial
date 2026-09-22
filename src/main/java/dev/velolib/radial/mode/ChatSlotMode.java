package dev.velolib.radial.mode;

import dev.velolib.radial.api.RadialSlot;
import dev.velolib.radial.api.SlotActionContext;
import dev.velolib.radial.mode.base.IconEnabledSlotMode;
import dev.velolib.radial.ui.screen.SlotEditorScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;

public class ChatSlotMode extends IconEnabledSlotMode {

    @Override
    public Text getTranslatedName() {
        return Text.translatable("radial.mode.chat");
    }

    @Override
    public void buildEditorWidgets(
            SlotEditorScreen screen, RadialSlot slot, int width, DirectionalLayoutWidget container) {
        // Group the Label and EditBox together closely
        DirectionalLayoutWidget valueGroup = DirectionalLayoutWidget.vertical().spacing(2);

        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        TextWidget label = new TextWidget(Text.translatable("screen.radial.editor.value"), textRenderer);
        valueGroup.add(label);

        TextFieldWidget valueField =
                new TextFieldWidget(textRenderer, 0, 0, width, 20, Text.translatable("screen.radial.editor.value"));
        valueField.setMaxLength(Integer.MAX_VALUE);
        valueField.setText(slot.value != null ? slot.value : "");
        valueField.setChangedListener(v -> slot.value = v);
        valueGroup.add(valueField);

        // Add the group to the main container
        container.add(valueGroup);

        // 3. Icon Row
        buildIconRow(screen, slot, width, container);
    }

    @Override
    public void performAction(RadialSlot slot, SlotActionContext context) {
        context.closeScreen(); // Close the radial menu first

        if (slot.value == null || slot.value.isEmpty()) return;

        // Execute the chat message or command
        ClientPlayNetworkHandler connection = MinecraftClient.getInstance().getNetworkHandler();
        if (connection != null) {
            if (slot.value.startsWith("/")) {
                connection.sendChatCommand(slot.value.substring(1)); // Remove the slash for commands
            } else {
                connection.sendChatMessage(slot.value); // Send as normal chat
            }
        }
    }
}
