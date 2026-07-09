package com.safhy.chatlookup.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatHud.class)
public interface ChatHudAccessor {
    @Accessor("messages")
    List<ChatHudLine> chatlookup$getMessages();

    @Accessor("visibleMessages")
    List<ChatHudLine.Visible> chatlookup$getVisibleMessages();

    @Accessor("scrolledLines")
    int chatlookup$getScrolledLines();

    @Invoker("refresh")
    void chatlookup$refresh();

    @Invoker("getChatScale")
    double chatlookup$getChatScale();

    @Invoker("getWidth")
    int chatlookup$getWidth();

    @Invoker("getLineHeight")
    int chatlookup$getLineHeight();

    @Invoker("isChatHidden")
    boolean chatlookup$isChatHidden();
}
