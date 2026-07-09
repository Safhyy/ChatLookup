package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatLookup;
import com.safhy.chatlookup.ChatMessageCopier;
import com.safhy.chatlookup.HighlightRenderer;
import com.safhy.chatlookup.SearchFieldWidget;
import com.safhy.chatlookup.ToggleButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
    protected TextFieldWidget chatField;

    @Unique
    private TextFieldWidget chatlookup$searchField;
    @Unique
    private ToggleButton chatlookup$highlightButton;

    protected ChatScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void chatlookup$addSearchField(CallbackInfo ci) {
        TextFieldWidget field = new SearchFieldWidget(this.textRenderer, 4, this.height - 30,
                SEARCH_FIELD_WIDTH, 12, Text.translatable("chatlookup.search"));
        field.setMaxLength(256);
        field.setPlaceholder(Text.translatable("chatlookup.search.hint")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
        field.setText(ChatLookup.getQuery());
        field.setChangedListener(ChatLookup::setQuery);
        this.chatlookup$searchField = field;
        this.addDrawableChild(field);

        int buttonY = this.height - 30;
        ToggleButton regexButton = new ToggleButton(field.getX() + field.getWidth() + 4, buttonY,
                Text.literal(".*"), Text.translatable("chatlookup.regex.tooltip"), false,
                ChatLookup::isRegexMode, () -> ChatLookup.setRegexMode(!ChatLookup.isRegexMode()));
        ToggleButton highlightButton = new ToggleButton(regexButton.getX() + ToggleButton.SIZE + 4, buttonY,
                Text.literal("A"), Text.translatable("chatlookup.highlight.tooltip"), true,
                ChatLookup::isHighlightEnabled, () -> ChatLookup.setHighlightEnabled(!ChatLookup.isHighlightEnabled()));
        this.chatlookup$highlightButton = highlightButton;
        this.addDrawableChild(regexButton);
        this.addDrawableChild(highlightButton);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void chatlookup$renderOverlays(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (this.client == null) {
            return;
        }
        TextFieldWidget field = this.chatlookup$searchField;
        if (field != null) {
            int counterX = this.chatlookup$highlightButton != null
                    ? this.chatlookup$highlightButton.getX() + ToggleButton.SIZE + 6
                    : field.getX() + field.getWidth() + 6;
            int counterY = field.getY() + 2;
            if (ChatLookup.isFiltering() && ChatLookup.isQueryInvalid()) {
                field.setEditableColor(TEXT_COLOR_NO_MATCH);
                context.drawTextWithShadow(this.textRenderer,
                        Text.translatable("chatlookup.regex.invalid"), counterX, counterY, TEXT_COLOR_NO_MATCH);
            } else if (ChatLookup.isFiltering()) {
                ChatHud chatHud = this.client.inGameHud.getChatHud();
                int matched = ChatLookup.getMatchedCount(chatHud);
                int total = ChatLookup.getTotalCount(chatHud);

                field.setEditableColor(matched > 0 ? TextFieldWidget.DEFAULT_EDITABLE_COLOR : TEXT_COLOR_NO_MATCH);

                Text counter = Text.translatable("chatlookup.matches", matched, total);
                context.drawTextWithShadow(this.textRenderer, counter, counterX, counterY, matched > 0 ? 0xFFA0FFA0 : TEXT_COLOR_NO_MATCH);

                if (ChatLookup.isHighlightEnabled()) {
                    HighlightRenderer.render(context, this.textRenderer, this.client, this.height);
                }
            } else {
                field.setEditableColor(TextFieldWidget.DEFAULT_EDITABLE_COLOR);
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void chatlookup$handleSearchKeys(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        TextFieldWidget field = this.chatlookup$searchField;
        if (field == null || !field.isFocused()) {
            return;
        }
        switch (input.key()) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                this.setFocused(this.chatField);
                cir.setReturnValue(true);
            }
            case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN -> cir.setReturnValue(true);
            case GLFW.GLFW_KEY_ESCAPE -> {
                if (!field.getText().isEmpty()) {
                    field.setText("");
                } else {
                    this.setFocused(this.chatField);
                }
                cir.setReturnValue(true);
            }
            default -> {
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void chatlookup$copyOnCtrlClick(Click click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && click.hasCtrlOrCmd() && this.client != null
                && ChatMessageCopier.copyMessageAt(this.client, click.x(), click.y(),
                        this.client.getWindow().getScaledHeight())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void chatlookup$clearFilterOnClose(CallbackInfo ci) {
        ChatLookup.setQuery("");
    }
}
