# Fabric Mod Direct Update Guide to 1.21.10

This guide provides a summary of the necessary changes to update a Fabric mod from an older version directly to 1.21.10, without detailing intermediate steps that have become obsolete.

## General Recommendations

- **Patience with Mod Developers:** Please be patient and give mod developers time to update to new versions.
- **World Backups:** Always make backups of your worlds before updating.
- **Yarn Mappings:** All code references use Yarn mappings.

## Key Changes and How to Update

### Development Environment

- **Loom:** Use Loom 1.11 or newer.
- **Fabric Loader:** Use Fabric Loader 0.17.2 or newer.
- **Java:** Continue to use Java 21 for mod development and gameplay.

### Fabric API and Loader Changes

- **Module Removals and Merges:** Several modules have been removed or merged. Check your `build.gradle` for dependencies on removed modules like `fabric-command-api-v1`, `fabric-keybindings-v0`, etc.
- **Mixin & MixinExtras:** Fabric Loader bundles MixinExtras 5.0.0 and Fabric Mixin 0.16.3+mixin.0.8.7. This introduces new features like `@WrapMethod`, `@Cancellable`, and expressions.
- **Tag and Registry Aliases:** Use the new tag and registry alias APIs to migrate old tags and registry entries to new names.

### Minecraft and Fabric API Breaking Changes

#### Rendering

- **Rendering Pipeline:** Minecraft is separating its rendering pipeline. Many methods in `RenderSystem` have been removed. Use `RenderPipeline`s with `RenderLayer`s instead.
- **World Render Events:** The old world render events have been removed. Use mixins for now.
- **HUD API:** The HUD API has been rewritten. Use `HudElementRegistry` to add custom HUD elements.
- **`BlockRenderLayerMap` API:** The API has been updated. Use `BlockRenderLayerMap.putBlock(...)` instead of accessing the `INSTANCE`.
- **Entity Rendering:** Entity rendering has been refactored to use a `RenderState` object. Update your entity renderers to use `createRenderState` and `updateRenderState`.
- **`OrderedRenderCommandQueue`:** Most world rendering now uses `OrderedRenderCommandQueue`. Update your block entity, particle, and entity rendering to use this queue.

#### NBT

- **`ReadView` and `WriteView`:** `BlockEntity`s now use `ReadView` and `WriteView` for NBT serialization. Update your block entities to use `writeData` and `readData`.
- **`Optional` Return Values:** `NbtCompound` methods now return `Optional`. Update your code to handle `Optional` values.

#### Blocks and Items

- **Registry Keys:** You must manually set registry keys in the settings of every block and item.
- **Item Models:** Items now use item model definition JSON files in `assets/<namespace>/items`.
- **Block Models:** All block entities now render their block models.
- **Pick Item Events:** Use the new server-side `PlayerPickItemEvents#BLOCK` and `PlayerPickItemEvents#ENTITY` events.
- **`FabricElytraItem` Removed:** Use the `minecraft:glider` item component instead.

#### Entities

- **`EntityType` Registry Key:** Pass a `RegistryKey` when building an `EntityType`.
- **`ServerWorld` Parameters:** Many server-side methods now require a `ServerWorld` parameter. Check for `world instanceof ServerWorld` before calling these methods.
- **Spawn Reason:** A spawn reason is now required when creating an entity.

#### Other Breaking Changes

- **Resource Loader API v1:** The resource loader API has been reworked. Register reloaders with an identifier directly using `ResourceLoader.get(...).registerReloader(...)`.
- **`ActionResult`:** `ItemActionResult` and `TypedActionResult` have been merged into `ActionResult`.
- **Data Generation:** Override `getRecipeGenerator` in your recipe providers. Use `valueLookupBuilder` instead of `getOrCreateTagBuilder`.
- **Keybinding Changes:** Keybinding categories are now more structured. Use `KeyBinding.Category.create(...)`.

### New APIs and Features

- **Client Game Test API:** A new experimental API for testing client rendering and GUI.
- **ComponentTooltipAppenderRegistry:** A new way to append tooltips to items.
- **LootTableEvents.MODIFY_DROPS:** A new event to modify the collective output of `LootTable`s.
- **ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE:** An event that fires for changes in chunk loading level.
- **Attachment Change Event:** An event for reacting to attachment value changes.
- **ServerPlayerEvents.JOIN and LEAVE:** Events for player initialization and de-initialization.
- **FabricSoundsProvider:** A new class for creating `sounds.json` in datagen.
- **FabricTrackedDataRegistry:** A new registry for tracked data handlers.
- **Debug Text API:** A new API to register debug HUD entries.
