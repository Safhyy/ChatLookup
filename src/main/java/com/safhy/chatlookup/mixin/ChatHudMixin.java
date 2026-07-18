package com.safhy.chatlookup.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.safhy.chatlookup.ChatAnimator;
import com.safhy.chatlookup.ChatHeads;
import com.safhy.chatlookup.ChatHistoryStore;
import com.safhy.chatlookup.ChatLookup;
import com.safhy.chatlookup.MessageDecorator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"), cancellable = true)
    private void chatlookup$filterVisibleMessage(GuiMessage message, CallbackInfo ci) {
        ChatLookup.markCountsDirty();
        if (MessageDecorator.onDisplayAdd((ChatComponent) (Object) this, message)) {
            ci.cancel();
            return;
        }
        if (!ChatLookup.matches(message) || ChatLookup.consumeRefreshSkip()) {
            ci.cancel();
            return;
        }
        ChatHeads.beginLines(message);
        if (!MessageDecorator.isRefreshing() && !MessageDecorator.isApplyingStack()) {
            ChatAnimator.beginMessageAdd();
        }
    }

    @Inject(method = "addMessageToDisplayQueue", at = @At("RETURN"))
    private void chatlookup$endLineContext(GuiMessage message, CallbackInfo ci) {
        ChatHeads.endLines();
        ChatAnimator.endMessageAdd();
    }

    @Inject(method = "addMessageToQueue", at = @At("HEAD"), cancellable = true)
    private void chatlookup$redirectQueueAdd(GuiMessage message, CallbackInfo ci) {
        if (MessageDecorator.consumeSkipQueueAdd()) {
            ci.cancel();
            return;
        }
        GuiMessage replacement = MessageDecorator.consumeQueueReplacement();
        if (replacement != null) {
            ci.cancel();
            ((ChatHudAccessor) this).chatlookup$addToQueue(replacement);
        }
    }

    @Inject(method = "addMessageToQueue", at = @At("TAIL"))
    private void chatlookup$persistMessage(GuiMessage message, CallbackInfo ci) {
        ChatHistoryStore.append(message.content());
    }

    //? if >=26.1 {
    @WrapOperation(method = "addMessageToDisplayQueue",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/chat/GuiMessage;splitLines(Lnet/minecraft/client/gui/Font;I)Ljava/util/List;"))
    private List<FormattedCharSequence> chatlookup$indentWrappedLines(GuiMessage message, Font font, int width,
                                                                     Operation<List<FormattedCharSequence>> original) {
        return MessageDecorator.indentContinuationLines(message.content(), font, width,
                reduced -> original.call(message, font, reduced));
    }
    //?}
    //? if >=1.21.9 && <26.1 {
    /*@WrapOperation(method = "addMessageToDisplayQueue",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/GuiMessage;splitLines(Lnet/minecraft/client/gui/Font;I)Ljava/util/List;"))
    private List<FormattedCharSequence> chatlookup$indentWrappedLines(GuiMessage message, Font font, int width,
                                                                     Operation<List<FormattedCharSequence>> original) {
        return MessageDecorator.indentContinuationLines(message.content(), font, width,
                reduced -> original.call(message, font, reduced));
    }
    *///?}
    //? if <1.21.9 {
    /*@WrapOperation(method = "addMessageToDisplayQueue",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"))
    private List<FormattedCharSequence> chatlookup$indentWrappedLines(FormattedText text, int width, Font font,
                                                                      Operation<List<FormattedCharSequence>> original) {
        if (!(text instanceof Component component)) {
            return original.call(text, width, font);
        }
        return MessageDecorator.indentContinuationLines(component, font, width,
                reduced -> original.call(text, reduced, font));
    }
    *///?}

    @Inject(method = "refreshTrimmedMessages", at = @At("HEAD"))
    private void chatlookup$beginRefresh(CallbackInfo ci) {
        MessageDecorator.setRefreshing(true);
    }

    @Inject(method = "refreshTrimmedMessages", at = @At("RETURN"))
    private void chatlookup$endRefresh(CallbackInfo ci) {
        MessageDecorator.setRefreshing(false);
    }

    @Unique
    private List<GuiMessage> chatlookup$survivors;

    @Inject(method = "clearMessages", at = @At("HEAD"))
    private void chatlookup$captureBeforeClear(boolean clearMessageHistory, CallbackInfo ci) {
        if (clearMessageHistory) {
            this.chatlookup$survivors = List.copyOf(((ChatHudAccessor) this).chatlookup$getMessages());
        }
    }

    @Inject(method = "clearMessages", at = @At("TAIL"))
    private void chatlookup$afterClear(boolean clearMessageHistory, CallbackInfo ci) {
        if (clearMessageHistory) {
            List<GuiMessage> messages = ((ChatHudAccessor) this).chatlookup$getMessages();
            for (GuiMessage line : this.chatlookup$survivors) {
                messages.add(ChatHistoryStore.restoredLine(line.content()));
            }
            this.chatlookup$survivors = null;
            ChatLookup.budgetedRefresh((ChatComponent) (Object) this);
        } else {
            ChatHistoryStore.clearFile();
            ChatLookup.markCountsDirty();
        }
    }

    @ModifyConstant(
            method = {"addMessageToQueue", "addMessageToDisplayQueue"},
            constant = @Constant(intValue = 100),
            require = 0,
            expect = 0
    )
    private int chatlookup$raiseHistoryLimit(int vanillaLimit) {
        return Math.max(vanillaLimit, ChatLookup.HISTORY_LIMIT);
    }

    //? if >=26.1 {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("HEAD"))
    private void chatlookup$pushChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
    //?}
    //? if >=1.21.9 && <26.1 {
    /*@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V", at = @At("HEAD"))
    private void chatlookup$pushChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics graphics) {
    *///?}
    //? if <1.21.9 {
    /*@Inject(method = "render", at = @At("HEAD"))
    private void chatlookup$pushChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics graphics) {
    *///?}
        ChatAnimator.pushChatPose(graphics, (ChatComponent) (Object) this);
    }

    //? if >=26.1 {
    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V", at = @At("RETURN"))
    private void chatlookup$popChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphicsExtractor graphics) {
    //?}
    //? if >=1.21.9 && <26.1 {
    /*@Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;IIIZZ)V", at = @At("RETURN"))
    private void chatlookup$popChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics graphics) {
    *///?}
    //? if <1.21.9 {
    /*@Inject(method = "render", at = @At("RETURN"))
    private void chatlookup$popChatAnimation(CallbackInfo ci, @Local(argsOnly = true) GuiGraphics graphics) {
    *///?}
        ChatHeads.render(graphics, (ChatComponent) (Object) this, Minecraft.getInstance());
        ChatAnimator.popChatPose(graphics);
    }

    //? if >=1.21.6 {
    @ModifyVariable(method = "forEachLine", at = @At("STORE"), ordinal = 0)
    private float chatlookup$fadeInLine(float original, @Local GuiMessage.Line line) {
        return (float) ChatAnimator.easeLineOpacity(original, line);
    }
    //?} else {
    /*@ModifyVariable(method = "render", at = @At("STORE"), ordinal = 3)
    private double chatlookup$fadeInLine(double original, @Local GuiMessage.Line line) {
        return ChatAnimator.easeLineOpacity(original, line);
    }
    *///?}
}
