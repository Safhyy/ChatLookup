package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatLookup;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatComponent.class)
public abstract class ChatHeightMixin {
    @Inject(method = "getHeight(D)I", at = @At("HEAD"), cancellable = true)
    private static void chatlookup$extendChatHeight(double percent, CallbackInfoReturnable<Integer> cir) {
        if (ChatLookup.getMaxChatHeight() <= ChatLookup.VANILLA_CHAT_HEIGHT) {
            return;
        }
        cir.setReturnValue(ChatLookup.chatHeight(percent));
    }
}
