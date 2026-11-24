# Languages

[EN/English](https://lyuwenhan.github.io/extensions/minecraft-java/wenhan-client/README/README.html) | [ZH-CN/简体中文](https://lyuwenhan.github.io/extensions/minecraft-java/wenhan-client/README/README_ZH-CN.html)

# Wenhan Client

Wenhan Client is a collection of independent features designed to make Minecraft easier to control, explore, build, and customize.

## Features

### General

| Feature | Description |
| --- | --- |
| Password Hiding Mode | For supported login and account commands, disable masking, hide password contents with a matching number of asterisks, or also hide password length with a fixed eight-character mask. |
| Hide Realms | Disables Realms background checks and hides the Minecraft Realms button on the title screen. |
| Instant Respawn | Skips the death screen and respawns immediately. |
| Block Explosion Guard | Prevents using beds or straw beds outside the Overworld, and respawn anchors outside the Nether. Mining is not blocked. |
| Safe Drop | Requires a second drop key press to confirm dropping protected items in game. |
| No Camera Collision | Keeps the third-person camera distance fixed instead of shortening it when blocks are behind you. |
| Attack Through Blocks | Off: no block penetration. Attack Through Foliage: target through blocks without collision, such as grass and flowers. Attack Through All Blocks: ignore all blocks and prioritize living entities along the view ray within attack reach, keeping the normal hit when none is found. |
| No Fog | Removes water, lava, powdered snow, blindness, darkness, weather, boss, dimension terrain fog, the void sky mask, low-altitude sky darkening, biome sky tints, sky color changes in fluids, and FOV distortion. |
| Always Render Barriers | Keeps barrier, structure void, and light blocks visible. |
| No Mining Delay | While holding attack on blocks, lowers repeat mining delay to 1 tick. |
| No Item Use Delay | While holding use on blocks, lowers repeat use/place delay to 1 tick. |
| Stable Mining | While mining a block, keeps your crosshair aimed at that block's center unless the line of sight is blocked. |
| Scaffold Placement Assist | While holding a block and not targeting a real block, allows the crosshair to target placeable cells one block horizontally outward from blocks with a selectable outline. Fluids are not expansion anchors, and real block hits always take priority. |
| Liquid Surface Placement | While holding a block and not targeting a real block: when inside the matching liquid, targets liquid cells touching air; otherwise, targets air cells touching that liquid. This only supplies extra crosshair targets and never blocks placement allowed by other behavior. |
| Continuous Placement Constraint | While the use key is held, subsequent placements are constrained relative to the first successfully placed block. Axial Line uses the second block to select the X, Y, or Z axis; Horizontal Plane fixes the Y coordinate; Vertical Plane uses the first horizontal offset to select a fixed-X or fixed-Z plane. The crosshair only targets placeable positions intersected by the view ray, including air blocks. Releasing the use key resets the placement sequence. |
| Player Highlight Mode | Controls when player highlighting is active. |
| Player and Waypoint Information HUD | Shows nearby player and waypoint information on the HUD. |

Player highlighting supports Disabled, Only While Holding Tab, and Always Enabled modes. The HUD can show names, relative directions, horizontal distances, health, and coordinates.

Example:

```text
Player867 ↑ 11m ❤ 20.0 (123, 64, -456)
```

### Movement

| Feature | Description |
| --- | --- |
| Creative Flying | Enables flight in Survival and Adventure mode. |
| Disable Creative Flight | Prevents creative flight from starting and stops active creative flight. |
| Disable Elytra Flight | Prevents elytra flight from starting and stops active elytra gliding. |
| No Flight Reset | Keeps creative flight enabled when touching the ground. |
| Freecam Interaction Rotation | Turns the local player toward interaction targets when using Freecam interactions. Optionally restores the original view afterward. |
| Wall Climb | Lets only the local player climb walls of any block. Hold jump next to a wall or move against it to climb upward. |
| Elytra Cancel | Press jump while elytra gliding to stop fall flying. |
| No Jump Delay | Removes the jump cooldown so holding jump can trigger consecutive jumps immediately. |
| No Levitation | Cancels levitation for the local player. |
| No Slow Falling | Cancels slow falling for the local player. |
| No Collision | Prevents other entities from pushing the local player. |
| Fluid Platform | Allows the local player to stand on water and lava surfaces. Sneak to pass through. |
| Solid Cobweb | Gives cobwebs a full-block collision box only for the local player without affecting Attack Through Blocks. |
| Void Floor | Creates a one-way floor at Y=-105 in the Overworld and Y=-41 in the Nether and End. Requires flight permission and only blocks downward movement. |
| No Slowdown | Removes movement slowdown while using shields, bows, crossbows, tridents, spyglasses, goat horns, food, and drink items, and removes the horizontal speed limit while climbing. |
| No Slime Bounce | Prevents slime blocks from bouncing the local player. |
| Follow Minecart Direction | When the current minecart turns, applies the same change to the player view. |
| Air Step Up | Allows player step-up handling while airborne when colliding with small ledges. |
| Step Down | Allows smooth controlled step-down when moving toward reachable lower ground. |
| Step Height | Sets the step height. |
| Sneaking Step Height | Sets the step height used while sneaking. |
| Override Jump Height | Overrides the local player's jump strength using the configured target jump height. |
| Target Jump Height | Saved target jump height in blocks. The jump strength is calculated at runtime from this height and the player's gravity. |
| Infinite Jumps | Allows unlimited jumps while airborne. |
| Jump Count | Sets the total number of jumps available before landing. Set to 1 to disable extra jumps. |
| Double Jump Cooldown | Requires the configured number of ticks between additional airborne jumps. |
| Double Jump Cooldown Ticks | Sets the number of ticks required between additional airborne jumps. Minimum: 0. |
| Coyote Time | Allows a ground jump for this many ticks after walking or falling off an edge. Minimum: 0. |
| Delayed Falling Time | After walking off a block edge, keeps the player at the same height for this many ticks. |

### Auto Greet

| Feature | Description |
| --- | --- |
| Greet on Your Join | Sends your configured messages in order after you join a server. |
| Greet Other Players | Greets joining players according to the blacklist and whitelist rules. |

Messages can use `@player`, `@UUID`, `@X`, `@Y`, `@Z`, `@health`, and `@level` placeholders.

### Auto Login

| Feature | Description |
| --- | --- |
| Enable Auto Login | Logs in with the saved password after joining this server. A password must first be saved for this server. |
| Set or Change Password | Enter a new password and save. The password is never populated when this screen opens; leaving it blank keeps the saved password. The field is cleared after saving. |
| Clear Saved Password | Immediately deletes the locally saved password for this server. This button is unavailable when no password is saved. |
| Delete This Server's Login Settings | Immediately removes this server entry, its saved password, and its enabled state. |

Passwords are encrypted on the device and are not displayed again after saving.

### Who I Am

| Feature | Description |
| --- | --- |
| Show Own Name Tag | Shows your own player name tag in third-person or free-camera views. |
| Name in Window Title | Adds your player name to the Minecraft window title. |

### No Texture Rotation

| Feature | Description |
| --- | --- |
| Enabled | Disables position-based random block model variants such as random texture rotation. |
| Disable Random Offset | Removes random visual position offsets from blocks such as grass and bamboo. |
| Keep Collision Offsets | Keeps visual offsets for blocks with a non-empty collision shape. |

### Fly Speed Modifier

| Feature | Description |
| --- | --- |
| Enabled | Enables flight speed multiplier adjustment. |
| Maximum Speed | Saved upper bound for the temporary flight speed multiplier. Minimum: 1.00x. Recommended: 100.00x or lower. |
| Minimum Speed | Lower bound for the temporary flight speed multiplier. Supported range: 0.00x to 1.00x. |
| Default Speed | Saved multiplier used for the first adjustment and for reset-on-adjust. Minimum: the configured minimum speed. Maximum: the configured maximum speed. |
| Reset Before Adjustment | When enabled, pressing the adjustment key resets the multiplier to the configured default value. When disabled, the previous multiplier is preserved. |
| Multiplier Step | Controls how many non-linear multiplier adjustment operations are applied per mouse-wheel unit. |
| Apply to Other Movement | Also applies the current multiplier to client-controlled movement. |

### Boat Utils

| Feature | Description |
| --- | --- |
| Unlock View Rotation | Removes the vanilla 180-degree yaw restriction, allowing the camera to rotate freely in a full circle. |
| Follow View | Makes the boat follow the view direction. Left and right movement keys strafe horizontally instead of turning the boat. |
| Auto Recenter Camera | When Follow View is disabled, smoothly recenters the camera after 1.5 seconds without mouse camera movement. |
| Direction Hotkeys | Enables the four boat direction shortcuts. Up snaps to the nearest allowed direction, Down turns around, and Left or Right turns to the next allowed direction on that side. |
| Diagonal Directions | When enabled, allowed directions are every multiple of 45 degrees. When disabled, only the four cardinal directions are allowed. |
| Increase Boat Speed | Makes the boat use blue ice friction at all times. |
| Prevent Sinking | While the boat is underwater, sets an upward speed to prevent it from sinking. |
| Boat Step Height | Sets the maximum height the boat can step up while moving. Range: 0.0 to 10.0 blocks. |
| Enable Handbrake | While controlling a boat, holding the jump key applies the handbrake. |
| Enable Release Boost | If enabled, after releasing the handbrake, adds a boost based on how long the handbrake was held. |
| Enable Lateral Friction | Applies additional friction to lateral boat movement. |

### Entity Highlighter

| Feature | Description |
| --- | --- |
| Entity Groups | Configures highlighting for attackable entities, dropped items, animals, monsters, or all entities. Specific entity rules take priority over standard group rules. |
| Entity Types | Configures highlighting and color for individual entity types. Dropped items are handled by Item Highlight. |

### Auto Aim

| Feature | Description |
| --- | --- |
| Auto Aim Mode | Controls when a target is locked. A target is released after leaving the current entity interaction range. |
| Restore View After Aiming | Records the view before auto aim begins and restores it under the selected condition when the target is lost or the lock ends. |
| Maximum Auto-Attack Distance | Auto aim only locks creatures within this distance. The effective range is also limited by the current main-hand item's attack range. Distance is measured from the player's viewpoint to the nearest point of the target's hitbox. |
| Highlight Target | Shows a glowing outline around the currently locked auto-aim target. |
| Target Highlight Color | Sets the glowing outline color for the locked auto-aim target. |
| Automatic Attack | After Cooldown waits for full attack charge and then adds the delay. Enabled only applies the configured delay. The delay also applies to newly selected targets. |
| Automatic Attack Delay | Ticks to wait before an automatic attack, including the first selection and target switches. |

### Client Pseudo Unrestrict

| Feature | Description |
| --- | --- |
| No Block Interaction Range Limit | Extends the local client's block interaction range. Servers may still reject distant interactions. |
| No Entity Interaction Range Limit | Extends the local client's entity interaction range. Servers may still reject distant interactions. |
| Instant Break | Makes the local client calculate block breaking as instant. Servers may still correct the result. |

Multiplayer servers may still reject or correct actions that are not permitted by their rules.

### Other Features

| Feature | Description |
| --- | --- |
| Forward Lock | Double-tap forward while holding sprint to keep moving |
| Find | Finds loaded mobs or blocks by ID |
| Ghostblock | Adds client-side ghost items for testing |
| Best Armor | Adds prepared equipment boxes to the operator creative inventory |
| Map Locator | Turns markers from a held map into tracked waypoints |
| Numeric Enchantment Levels | Shows enchantment levels as numbers |
| Boat Item Visibility | Keeps held items visible while controlling a boat |
| Remove Command Length Limit | Allows longer text in the command input box |
| Leave Button | Leaves the current world with one key |

## Default Controls

| Control | Action |
| --- | --- |
| V | Toggle Creative Flying |
| B | Toggle Flight Disablers |
| Left Alt + Mouse Wheel | Adjust flight speed |
| I | Toggle Player Highlighter keep mode |
| TAB | Hold to highlight players |
| F8 | Toggle password hiding |
| Grave Accent (`) | Leave the current world |
| Arrow Keys | Control boat direction shortcuts when enabled |

## Commands

| Command | Description |
| --- | --- |
| `/wc autologin login` | Tries the saved login for the current server |
| `/wc findmob` | Finds loaded entities of the selected type |
| `/wc findblock` | Finds loaded blocks of the selected type |
| `/get` | Adds a ghost item |
| `/i` | Shows the current player identity |

## Supported Versions

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.5+
- Java 25
- Mod Menu (Optional)
- YetAnotherConfigLib (Optional)
- Do a Barrel Roll (Optional)
- Freecam (Optional)

## License

MIT
