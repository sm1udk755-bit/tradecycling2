# TradeCycling (Paper Plugin)

A Paper plugin recreation of the Fabric/Forge mod ["Trade Cycling"](https://www.curseforge.com/minecraft/mc-mods/trade-cycling)
by henkelmax. Server-side only, no client mod required.

## What it does

Sneak + right-click a villager to reroll its trade offers, without needing
to break and replace its workstation block.

Under the hood it uses the exact same mechanism vanilla uses when a
villager claims a workstation: the profession is briefly set to `NONE`
and immediately back to the original. Pure Bukkit/Paper API triggers the
trade regeneration itself — no NMS hacks needed, which makes the plugin
portable across versions.

## Building

Requirements: Maven + JDK 17+.

```bash
mvn clean package
```

The finished jar will be at `target/tradecycling-1.0.0.jar`.
Drop this file into your Paper server's `plugins` folder and restart the server.

**Important:** Adjust the `paper-api` version in `pom.xml` to match your
server version (e.g. `1.21.1-R0.1-SNAPSHOT`), so `api-version` in
`plugin.yml` and the API calls stay compatible.

## Configuration (`config.yml`)

| Option | Description |
|---|---|
| `require-sneak` | Whether the player must hold Shift |
| `required-item` | Optional item that must be held in hand (e.g. `STICK`) |
| `cooldown-seconds` | Cooldown per villager |
| `reset-uses` | Also resets each trade's "already used" counter |
| `success-sound` / `success-particle` | Feedback on a successful reroll |

## Commands & Permissions

- `/tradecycling reload` – reload the config (`tradecycling.admin`, default: op)
- `tradecycling.use` – allows rerolling trades (default: all players)

## Difference from the original mod

The original mod was built as a client/server Fabric/Forge mod with access
to internal Minecraft classes. This port is a pure server plugin (no client
mod needed) using only public Bukkit/Paper API, so it relies on the
profession-reset trick described above instead of tapping the same internal
logic 1:1. Behavior is practically identical.
