package com.safhy.chatlookup.mixin;

import net.minecraft.client.gui.components.ChatComponent;
//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(ChatComponent.class)
public interface ChatHudAccessor {
    @Accessor("allMessages")
    List<GuiMessage> chatlookup$getMessages();

    @Accessor("trimmedMessages")
    List<GuiMessage.Line> chatlookup$getVisibleMessages();

    @Accessor("chatScrollbarPos")
    int chatlookup$getScrolledLines();

    @Invoker("rescaleChat")
    void chatlookup$refresh();

    @Invoker("getScale")
    double chatlookup$getChatScale();

    @Invoker("getWidth")
    int chatlookup$getWidth();

    @Invoker("getLineHeight")
    int chatlookup$getLineHeight();
}
