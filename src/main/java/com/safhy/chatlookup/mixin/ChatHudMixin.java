package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatHistoryStore;
import com.safhy.chatlookup.ChatLookup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void chatlookup$restoreHistory(MinecraftClient client, CallbackInfo ci) {
        ChatHistoryStore.restoreInto((ChatHud) (Object) this);
    }

    @Inject(method = "addVisibleMessage", at = @At("HEAD"), cancellable = true)
    private void chatlookup$filterVisibleMessage(ChatHudLine message, CallbackInfo ci) {
        ChatLookup.markCountsDirty();
        if (!ChatLookup.matches(message) || ChatLookup.consumeRefreshSkip()) {
            ci.cancel();
        }
    }

    @Inject(method = "addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", at = @At("TAIL"))
    private void chatlookup$persistMessage(ChatHudLine message, CallbackInfo ci) {
        ChatHistoryStore.append(message.content());
    }

    @Unique
    private List<ChatHudLine> chatlookup$survivors;

    @Inject(method = "clear", at = @At("HEAD"))
    private void chatlookup$captureBeforeClear(boolean clearMessageHistory, CallbackInfo ci) {
        if (clearMessageHistory) {
            this.chatlookup$survivors = List.copyOf(((ChatHudAccessor) this).chatlookup$getMessages());
        }
    }

    @Inject(method = "clear", at = @At("TAIL"))
    private void chatlookup$afterClear(boolean clearMessageHistory, CallbackInfo ci) {
        if (clearMessageHistory) {
            List<ChatHudLine> messages = ((ChatHudAccessor) this).chatlookup$getMessages();
            for (ChatHudLine line : this.chatlookup$survivors) {
                messages.add(new ChatHudLine(ChatHistoryStore.RESTORED_CREATION_TICK, line.content(), null, null));
            }
            this.chatlookup$survivors = null;
            ChatLookup.budgetedRefresh((ChatHud) (Object) this);
        } else {
            ChatHistoryStore.clearFile();
            ChatLookup.markCountsDirty();
        }
    }

    @ModifyConstant(
            method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V", "addVisibleMessage"},
            constant = @Constant(intValue = 100),
            require = 0,
            expect = 0
    )
    private int chatlookup$raiseHistoryLimit(int vanillaLimit) {
        return Math.max(vanillaLimit, ChatLookup.HISTORY_LIMIT);
    }
}
