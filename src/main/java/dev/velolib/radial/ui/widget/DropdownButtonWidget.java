package dev.velolib.radial.ui.widget;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DropdownButtonWidget<T> extends AbstractWidget {
    private static final ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath("minecraft", "widget/text_field");
    private static final ResourceLocation SPRITE_HIGHLIGHTED =
            ResourceLocation.fromNamespaceAndPath("minecraft", "widget/text_field_highlighted");
    private final List<T> options;
    private final Function<T, Component> labelMapper;
    private final Consumer<T> onSelect;
    private final Consumer<DropdownMenuWidget<T>> menuRegistrar;
    private T selectedOption;
    private DropdownMenuWidget<T> activeMenu = null;

    public DropdownButtonWidget(
            int x,
            int y,
            int width,
            int height,
            List<T> options,
            T initialSelection,
            Function<T, Component> labelMapper,
            Consumer<T> onSelect,
            Consumer<DropdownMenuWidget<T>> menuRegistrar) {
        super(x, y, width, height, labelMapper.apply(initialSelection));
        this.options = options;
        this.selectedOption = initialSelection;
        this.labelMapper = labelMapper;
        this.onSelect = onSelect;
        this.menuRegistrar = menuRegistrar;
    }

    public DropdownMenuWidget<T> getActiveMenu() {
        return this.activeMenu;
    }

    public boolean isMenuOpen() {
        return this.activeMenu != null;
    }

    public void closeMenu() {
        this.activeMenu = null;
    }

    public void updateSelection(T option) {
        this.selectedOption = option;
        this.setMessage(this.labelMapper.apply(option));
        this.onSelect.accept(option);
        this.closeMenu();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isActive() && this.isMouseOver(mouseX, mouseY)) {
            this.onClick(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        // PLAY SOUND: Triggers the standard click noise when toggling the menu
        this.playDownSound(Minecraft.getInstance().getSoundManager());

        if (isMenuOpen()) {
            closeMenu();
        } else {
            this.activeMenu = new DropdownMenuWidget<>(
                    getX(),
                    getY() + getHeight(),
                    this.width,
                    this.height,
                    this.options,
                    this.selectedOption,
                    this.labelMapper,
                    this);
            this.menuRegistrar.accept(this.activeMenu);
        }
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        boolean open = isMenuOpen();
        ResourceLocation currentSprite = (this.isFocused() || open) ? SPRITE_HIGHLIGHTED : SPRITE;

        graphics.blitSprite(currentSprite, getX(), getY(), width, height);

        int textColor = this.active ? 0xFFFFFFFF : 0xFFA0A0A0;
        Component currentText = this.labelMapper.apply(this.selectedOption);

        graphics.drawString(font, currentText, getX() + 4, getY() + (getHeight() - 8) / 2, textColor);
        graphics.drawString(
                font,
                Component.literal(open ? "▲" : "▼"),
                getX() + width - 12,
                getY() + (getHeight() - 8) / 2,
                textColor);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }
}
