# ChatLookup

A client-side Fabric mod that adds instant chat search, infinite chat history, match highlighting, and one-click message copying. Requires only Fabric Loader and Java 21 - no Fabric API.

## Features

**🔍 Instant search** - open the chat (`T`) and click the search box above the input. The chat filters live as you type: only matching messages stay visible, incoming messages are filtered too, and a `matched/total` counter tracks the results (red when nothing matches). Clearing the query or closing the chat restores the full history - nothing is ever lost.

**📜 Big, persistent history** - the chat remembers up to **32,768** messages (vanilla: 100) and survives both game restarts and server switches.

**`.*` Regex mode** - a toggle next to the search box turns the query into a case-insensitive regular expression.

**🖍 Match highlighting** - the `A` toggle marks every match inside visible messages with a translucent yellow marker.

**📋 Message copying** - `Ctrl+Left-click` any chat line to copy it to the clipboard. Also works while filtering - you copy exactly the message you see.

Localized in English, Spanish, Russian and Polish.

## Compatibility

Client-only - servers don't need to install anything. If another mod already raises the chat history limit, ChatLookup yields to it; search and copying still work over the full enlarged history.

## License

By Safhy - GNU GPL v3 (see `LICENSE`).
