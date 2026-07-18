package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.AnimatedLine;
import com.safhy.chatlookup.ChatAnimator;
import com.safhy.chatlookup.ChatHeads;
import com.safhy.chatlookup.ChatLookup;
import com.safhy.chatlookup.HeadedLine;
import net.minecraft.client.multiplayer.PlayerInfo;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
//?} else {
/*import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
*///?}
//? if >=1.21.9 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiMessage.Line.class)
public abstract class GuiMessageLineMixin implements AnimatedLine, HeadedLine {
    @Unique
    private long chatlookup$addedMs;
    @Unique
    private PlayerInfo chatlookup$headOwner;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void chatlookup$stampCreation(CallbackInfo ci) {
        this.chatlookup$addedMs = Util.getMillis();
        this.chatlookup$headOwner = ChatHeads.claimLineOwner();
        ChatAnimator.onLineCreated();
    }

    @Override
    public long chatlookup$addedMs() {
        return this.chatlookup$addedMs;
    }

    @Override
    public PlayerInfo chatlookup$headOwner() {
        return this.chatlookup$headOwner;
    }

    @Inject(method = "tag", at = @At("HEAD"), cancellable = true)
    private void chatlookup$hideIndicator(CallbackInfoReturnable<GuiMessageTag> cir) {
        if (ChatLookup.isIndicatorHidden()) {
            cir.setReturnValue(null);
        }
    }
}
