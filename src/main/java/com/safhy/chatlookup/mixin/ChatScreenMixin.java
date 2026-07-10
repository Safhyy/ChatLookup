package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatLookup;
import com.safhy.chatlookup.ChatMessageCopier;
import com.safhy.chatlookup.HighlightRenderer;
import com.safhy.chatlookup.SearchFieldWidget;
import com.safhy.chatlookup.ToggleButton;
import com.safhy.chatlookup.WidgetSkin;
import net.minecraft.ChatFormatting;
//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatScreen.class, priority = 900)
public abstract class ChatScreenMixin extends Screen {
    @Unique
    private static final int SEARCH_FIELD_WIDTH = 130;

    @Unique
    private static final int TEXT_COLOR_NO_MATCH = 0xFFFF5555;

    @Shadow
    protected EditBox input;

    @Unique
    private EditBox chatlookup$searchField;
    @Unique
    private ToggleButton chatlookup$highlightButton;
    @Unique
    private GuiEventListener chatlookup$focusBeforeClick;

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void chatlookup$addSearchField(CallbackInfo ci) {
        EditBox field = new SearchFieldWidget(this.font, 4, this.height - 30,
                SEARCH_FIELD_WIDTH, 12, Component.translatable("chatlookup.search"));
        field.setMaxLength(256);
        field.setHint(Component.translatable("chatlookup.search.hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        field.setValue(ChatLookup.getQuery());
        field.setResponder(ChatLookup::setQuery);
        this.chatlookup$searchField = field;
        this.addRenderableWidget(field);

        int buttonY = this.height - 30;
        ToggleButton regexButton = new ToggleButton(field.getX() + field.getWidth() + 4, buttonY,
                Component.literal(".*"), Component.translatable("chatlookup.regex.tooltip"), false,
                ChatLookup::isRegexMode, () -> ChatLookup.setRegexMode(!ChatLookup.isRegexMode()));
        ToggleButton highlightButton = new ToggleButton(regexButton.getX() + ToggleButton.SIZE + 4, buttonY,
                Component.literal("A"), Component.translatable("chatlookup.highlight.tooltip"), true,
                ChatLookup::isHighlightEnabled, () -> ChatLookup.setHighlightEnabled(!ChatLookup.isHighlightEnabled()));
        this.chatlookup$highlightButton = highlightButton;
        this.addRenderableWidget(regexButton);
        this.addRenderableWidget(highlightButton);
    }

    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void chatlookup$renderOverlays(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void chatlookup$renderOverlays(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    *///?}
        if (this.minecraft == null) {
            return;
        }
        EditBox field = this.chatlookup$searchField;
        if (field != null) {
            int counterX = this.chatlookup$highlightButton != null
                    ? this.chatlookup$highlightButton.getX() + ToggleButton.SIZE + 6
                    : field.getX() + field.getWidth() + 6;
            int counterY = field.getY() + 2;
            if (ChatLookup.isFiltering() && ChatLookup.isQueryInvalid()) {
                field.setTextColor(TEXT_COLOR_NO_MATCH);
                WidgetSkin.text(context, this.font,
                        Component.translatable("chatlookup.regex.invalid"), counterX, counterY, TEXT_COLOR_NO_MATCH, true);
            } else if (ChatLookup.isFiltering()) {
                ChatComponent chatHud = ChatLookup.getChat(this.minecraft);
                int matched = ChatLookup.getMatchedCount(chatHud);
                int total = ChatLookup.getTotalCount(chatHud);

                field.setTextColor(matched > 0 ? EditBox.DEFAULT_TEXT_COLOR : TEXT_COLOR_NO_MATCH);

                Component counter = Component.translatable("chatlookup.matches", matched, total);
                WidgetSkin.text(context, this.font, counter, counterX, counterY, matched > 0 ? 0xFFA0FFA0 : TEXT_COLOR_NO_MATCH, true);

                if (ChatLookup.isHighlightEnabled()) {
                    HighlightRenderer.render(context, this.font, this.minecraft, this.height);
                }
            } else {
                field.setTextColor(EditBox.DEFAULT_TEXT_COLOR);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    //? if >=1.21.9 {
    private void chatlookup$handleSearchKeys(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        int key = keyEvent.key();
    //?} else {
    /*private void chatlookup$handleSearchKeys(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        int key = keyCode;
    *///?}
        EditBox field = this.chatlookup$searchField;
        if (field == null || !field.isFocused()) {
            return;
        }
        switch (key) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                this.setFocused(this.input);
                cir.setReturnValue(true);
            }
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN -> cir.setReturnValue(true);
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (!field.getValue().isEmpty()) {
                    field.setValue("");
                } else {
                    this.setFocused(this.input);
                }
                cir.setReturnValue(true);
            }
            default -> {
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    //? if >=1.21.9 {
    private void chatlookup$copyOnCtrlClick(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        this.chatlookup$focusBeforeClick = this.getFocused();
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && click.hasControlDownWithQuirk() && this.minecraft != null
                && ChatMessageCopier.copyMessageAt(this.minecraft, click.x(), click.y(),
                        this.minecraft.getWindow().getGuiScaledHeight())) {
            cir.setReturnValue(true);
        }
    }
    //?} else {
    /*private void chatlookup$copyOnCtrlClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        this.chatlookup$focusBeforeClick = this.getFocused();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && Screen.hasControlDown() && this.minecraft != null
                && ChatMessageCopier.copyMessageAt(this.minecraft, mouseX, mouseY,
                        this.minecraft.getWindow().getGuiScaledHeight())) {
            cir.setReturnValue(true);
        }
    }
    *///?}

    // Toggle buttons must never steal typing focus; before 1.21.9 clicking the
    // vanilla chat box also does not focus it, so do that by hand as well.
    @Inject(method = "mouseClicked", at = @At("RETURN"))
    //? if >=1.21.9 {
    private void chatlookup$keepTypingFocus(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (this.getFocused() instanceof ToggleButton) {
            this.setFocused(this.chatlookup$focusBeforeClick);
        }
    }
    //?} else {
    /*private void chatlookup$keepTypingFocus(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.getFocused() instanceof ToggleButton) {
            this.setFocused(this.chatlookup$focusBeforeClick);
        } else if (mouseY >= this.height - 14) {
            this.setFocused(this.input);
        }
    }
    *///?}

    @Inject(method = "removed", at = @At("TAIL"))
    private void chatlookup$clearFilterOnClose(CallbackInfo ci) {
        ChatLookup.setQuery("");
    }
}
