##  Build

| Command | What it does |
|---|---|
| `.\gradlew.bat buildAndCollect` | Build **all** versions → jars land in `build/libs/1.1.0/` |
| `.\gradlew.bat :26.2:build` | Build a **single** version (jar in `versions/26.2/build/libs/`) |

##  Test in game

| Command | What it does |
|---|---|
| `.\gradlew.bat :26.2:runClient` | Launch a dev client for that MC version |
| `.\gradlew.bat :1.21.5:runClient` | …same, any target works (`:<version>:runClient`) |

##  Stonecutter - switch active version

The *active* version decides which `//? if` code branches are uncommented in the IDE.

| Command | What it does |
|---|---|
| `.\gradlew.bat "Set active project to 1.21.8"` | Swap sources to 1.21.8 (work/debug on that version) |
| `.\gradlew.bat "Reset active project"` | Back to the default (`26.2`)|

## Build targets

Each target builds one jar covering a range of MC versions (configured in `stonecutter.properties.toml`).

| Target | Covers |
| --- | --- |
| `1.21.1` | 1.21, 1.21.1|
| `1.21.5` | 1.21.2, 1.21.3, 1.21.4, 1.21.5 |
| `1.21.8` | 1.21.6, 1.21.7, 1.21.8 |
| `1.21.11` | 1.21.9, 1.21.10, 1.21.11 |
| `26.1.2` | 26.1, 26.1.1, 26.1.2 |
| `26.2` | 26.2 |