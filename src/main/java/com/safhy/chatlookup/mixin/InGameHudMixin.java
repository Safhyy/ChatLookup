package com.safhy.chatlookup.mixin;

import com.safhy.chatlookup.ChatMessageCopier;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} else {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=26.2 {
import net.minecraft.client.gui.Hud;
//?} else {
/*import net.minecraft.client.gui.Gui;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//? if >=26.2 {
@Mixin(Hud.class)
//?} else {
/*@Mixin(Gui.class)
*///?}
public abstract class InGameHudMixin {
    //? if >=26.1 {
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void chatlookup$renderCopyPopup(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
    //?} else {
    /*@Inject(method = "render", at = @At("TAIL"))
    private void chatlookup$renderCopyPopup(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
    *///?}
        ChatMessageCopier.renderCopyPopup(context, Minecraft.getInstance().font,
                context.guiWidth(), context.guiHeight());
    }
}
