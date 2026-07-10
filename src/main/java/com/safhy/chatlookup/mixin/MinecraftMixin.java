package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatHistoryStore;
import com.safhy.chatlookup.ChatLookup;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void chatlookup$restoreHistory(CallbackInfo ci) {
        Minecraft minecraft = (Minecraft) (Object) this;
        ChatHistoryStore.restoreInto(ChatLookup.getChat(minecraft));
    }
}
