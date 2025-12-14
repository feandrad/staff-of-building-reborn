# Staff of Building

**Staff of Building** is a Minecraft mod for **Fabric** and **NeoForge** that adds a variety of builder staffs (wands) to the game. These staffs significantly speed up construction by allowing you to place multiple blocks at once.

## ✨ Features

- **Connected Placement**: Places multiple blocks of the same type in a connected chain from the face you click.
- **Material Tiers**: Progression from Wooden to Infinite, with increasing range and durability.
- **Undo Functionality**: Made a mistake? Quickly undo your last placement.
- **Configurable**: Adjust placement limits, XP costs, and enable/disable specific staffs.

## 🛠️ Usage

1.  **Craft** a Builder's Staff.
2.  **Hold** the staff in your main hand.
3.  **Ensure** you have the blocks you want to place in your inventory (or offhand).
4.  **Right-click** on a block face to extend it. The staff will place connected blocks up to its limit.

### ↩️ Undo
While holding a Builder's Staff, press **`Ctrl + Z`** (or `Cmd + Z` on macOS) to undo the last placement operation.

## 🪄 Staff Tiers

| Staff | Max Blocks (Limit) | Durability | Notes |
| :--- | :---: | :---: | :--- |
| ![Wooden](fabric/src/main/resources/assets/staffofbuilding/textures/item/wooden_builder_staff.png) **Wooden** | 3 | 100 | |
| ![Stone](fabric/src/main/resources/assets/staffofbuilding/textures/item/stone_builder_staff.png) **Stone** | 6 | 250 | |
| ![Copper](fabric/src/main/resources/assets/staffofbuilding/textures/item/copper_builder_staff.png) **Copper** | 7 | 190 | |
| ![Iron](fabric/src/main/resources/assets/staffofbuilding/textures/item/iron_builder_staff.png) **Iron** | 9 | 500 | |
| ![Golden](fabric/src/main/resources/assets/staffofbuilding/textures/item/golden_builder_staff.png) **Golden** | 12 | 150 | High range, low durability |
| ![Diamond](fabric/src/main/resources/assets/staffofbuilding/textures/item/diamond_builder_staff.png) **Diamond** | 16 | 1000 | |
| ![Netherite](fabric/src/main/resources/assets/staffofbuilding/textures/item/netherite_builder_staff.png) **Netherite** | 32 | 1250 | Fire Resistant |
| ![Infinite](fabric/src/main/resources/assets/staffofbuilding/textures/item/infinite_builder_staff.png) **Infinite** | 64 | ∞ | Unbreakable |

## ⚙️ Configuration

The mod is highly configurable via `config/staffofbuilding.json`. You can modify:
- **Enabled**: Enable or disable specific staffs.
- **Size**: Change the maximum number of blocks each staff can place.
- **Experience Cost**: Set an XP cost per block placed (default: 1).

## 📜 License

This mod is licensed under the **MIT License**. You are free to use the code and assets as you see fit.

---
*Original mod by Draylar, maintained by contributors.*