package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatHistoryStore;
import com.safhy.chatlookup.ChatLookup;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatComponent.class)
public abstract class ChatHudMixin {
    @Inject(method = "addMessageToDisplayQueue", at = @At("HEAD"), cancellable = true)
    private void chatlookup$filterVisibleMessage(GuiMessage message, CallbackInfo ci) {
        ChatLookup.markCountsDirty();
        if (!ChatLookup.matches(message) || ChatLookup.consumeRefreshSkip()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessageToQueue", at = @At("TAIL"))
    private void chatlookup$persistMessage(GuiMessage message, CallbackInfo ci) {
        ChatHistoryStore.append(message.content());
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
}
