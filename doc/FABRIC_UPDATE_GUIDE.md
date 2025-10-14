# Fabric Mod Update Guide

This guide provides instructions on how to update your Fabric mod through various recent Minecraft versions.

## Updating to 1.21.2

Minecraft 1.21.2, the “Bundles of Bravery” drop, is expected to release soon. Mojang has recently [announced](https://www.minecraft.net/en-us/article/the-future-of-minecrafts-development) that they will release these “drops” throughout the year. Like with other updates, this drop contains some significant changes affecting mod makers.

As always, **we ask all players to be patient, and give mod developers time to update to this new version.** We ask everyone kindly not to pester them. **We also recommend all players to make a backup of their worlds.**

Here is a list of all major modder-facing changes in this version. Note that all code references are using Yarn mappings; modders using alternative mappings may need to use different names.

### Fabric changes

Developers should use Loom 1.8 (at the time of writing) to develop mods for Minecraft 1.21.2. Players should install the latest stable version of Fabric Loader (currently 0.16.7).

#### Fabric Loader changes

Fabric Loader 0.16.0 was released some time ago. Most notably, this release updates the bundled MixinExtras to 0.4.0.

In addition, Fabric Loader 0.16.7 added support for Java 23. However, you should continue to use Java 21 for mod development and gameplay.

##### MixinExtras additions

The following new features have been added:

- [`@WrapMethod`](https://github.com/LlamaLad7/MixinExtras/wiki/WrapMethod), which allows wrapping the entire method
- [`@Cancellable`](https://github.com/LlamaLad7/MixinExtras/wiki/Cancellable), which allows cancellation from all injectors.
- `namespace` parameter in [`@Share`](https://github.com/LlamaLad7/MixinExtras/wiki/Share) to share values between Mixins

#### Loom 1.8

Loom 1.8 adds support for configuration caches, Gradle 8.10, and other changes and fixes.

Configuration cache is an opt-in Gradle performance enhancement feature that will be enabled by default in Gradle 9. Because this may break existing setup, we only recommend using this if you are familiar with Gradle buildscripts. To support this change, multi-project optimization has been removed.

#### New Fabric API changes

With the help of many contributors, Fabric API has received some new features since the last update blog post:

- Convention Tags: Add more tags (TelepathicGrunt, Juuz)
- Crash Report Info: Print the full stack trace from the dedicated server watchdog (TelepathicGrunt)
- Entity Events: Add after damage event (TheDeathlyCow)
- Item API: Modify enchantment and add component map builder extensions (TheDeathlyCow)
- Item Group API: Add API to control creative inventory screen (modmuss50)
- Loot API: Loot API v3 (modmuss50)
- Networking: Add MinecraftClient/Server instances to networking contexts (modmuss50)
- Networking: Add `ClientConfigurationConnectionEvents#START` (modmuss50)
- Networking: Add access to ClientConfigurationNetworkHandler in context (Earthcomputer)
- Object Builder: Add an API to add additional supported blocks to block entity types (modmuss50)
- Renderer API: Add `ShadeMode` (PepperCode1)
- Renderer API: Quads overloads for joml interfaces (SHsuperCM)
- Resource Conditions: Allow conditions inside pack overlays (Apollo)
- Resource Loader: Add API to create reload listeners with a registry lookup (modmuss50)
- Removed Herobrine (Tiny Potato)
- Transfer API: Crafter support (modmuss50)
- Transfer API: Add `ItemVariant#withComponentChanges` (BasiqueEvangelist)

#### Breaking changes and deprecations

_Note: breaking changes related to vanilla changes are addressed separately below._

One deprecated module was removed: `fabric-renderer-registries-v1`.

In Resource Condition, the `test` method now takes `RegistryOps.RegistryInfoGetter`. Tags can no longer have resource conditions. Similarly, in Resource Loader, `ResourceReloadListenerKeys#TAGS` was removed.

The following deprecations were made since the last blogpost:

- Content Registries: `VillagerInteractionRegistries#registerCollectable` was deprecated. Use the vanilla tag, `minecraft:villager_picks_up`, instead.
- Loot API: Loot API v2 was deprecated. Use Loot API v3, which passes registry contexts, instead.
- Networking:`ClientConfigurationConnectionEvents#READY` was renamed to `COMPLETE`. (This was done during the 1.21 update cycle, but after we published the last blogpost.)

### Minecraft changes

#### `ServerWorld` parameters

Many methods that must be executed on the server side only now require `ServerWorld` to be passed explicitly. **Do not cast just to fix a type error;** many events and overridden method are still called on both the client side and the server side.

Instead, wrap all logic that must be run only on the server side with `if (world instanceof ServerWorld serverWorld)`. The `serverWorld` can be passed to those methods.

For example, `Entity#damage` now requires `ServerWorld`, and is therefore only called on the server side. There is an additional method, `clientDamage`, which is called only on the client side.

#### Block and item settings

Minecraft 1.21.2 uses registry keys to pre-compute certain block or item settings. This allows referencing them in default item components. For example, rather than computing a default name based on an item’s registry key inside `getName` method if the `minecraft:item_name` component is not present, every item now contains the `minecraft:item_name` component.

Because of this, you must **manually set registry keys in the settings of every block and item**. Failure to do so will result in crashes such as:

- `NullPointerException: Block id not set`
- `NullPointerException: Item id not set`

Both settings classes provide a simple method which should be called for every item or block with their respective registry key:

```
Identifier id = Identifier.of("mymod", "test_item");
RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);

Item.Settings settings = new Item.Settings()
    // If your item is based on a block
    .useBlockPrefixedTranslationKey()
    .registryKey(key);

Registry.register(Registries.ITEM, key, new Item(settings));
```

```
Identifier id = Identifier.of("mymod", "test_block");
RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, id);

Block.Settings settings = new Block.Settings()
    .registryKey(key);

Registry.register(Registries.BLOCK, key, new Block(settings));
```

Make sure to call the `Item.Settings#useBlockPrefixedTranslationKey` method for block items so that they use the `block.<namespace>.<path>` translation key format.

This change affects the following traits:

- Item names (`minecraft:item_name` component)
- Item models (`minecraft:item_model` component)
- Block models
- Block loot table key

Some traits, such as the cooldown group of items with cooldowns, still dynamically look up a registry key, but this pattern should be avoided as much as possible.

`Block#getLootTableKey` now returns `Optional<RegistryKey>`. A block without a corresponding loot table now returns an empty optional instead of the `minecraft:empty` loot table registry key.

In Fabric API, the previously-deprecated `FabricBlockSettings` class was removed. Use the vanilla `AbstractBlock.Settings` class instead.

#### Furnace fuels

Furnace fuels are now registered through an event, which allows access to new parameters:

```
- FuelRegistry.INSTANCE.add(ModItems.TEST_ITEM, 50);
+ FuelRegistryEvents.BUILD.register((builder, context) -> {
+     builder.add(ModItems.TEST_ITEM, context.baseSmeltTime() / 4);
+ });
```

The base smelt time can be used to express a fuel’s smelt time in terms of a ratio. The default base smelt time is 200 ticks, or 10 seconds.

#### Entities

`EntityType` received a registry key change similar to the block and item one described above. When building an `EntityType`, pass a `RegistryKey` for the entity type. The no-argument version of the `EntityType.Builder#build` method, injected by `FabricEntityTypeBuilder`, has been removed. For example:

```
Identifier id = Identifier.of("mymod", "test_entity_type");
RegistryKey<EntityType<?>> key = RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);

EntityType<TestEntity> entityType = EntityType.Builder.<TestEntity>create(TestEntity::new, SpawnGroup.MISC)
    .build(key);

Registry.register(Registries.ENTITY_TYPE, key, entityType);
```

Mob conversion (such as zombie villager curing or slimes splitting on death) was overhauled. The exact behavior of conversion is now handled by methods implemented by values of the `EntityConversionType` enum. When using the `MobEntity#convertTo` method to convert a mob, an `EntityConversionContext` is now required. This context is also now passed to Fabric API’s `ServerLivingEntityEvents#MOB_CONVERSION` event.

When creating an entity, a spawn reason is now required:

```
- Entity pig = EntityType.PIG.create(overworld);
+ Entity pig = EntityType.PIG.create(overworld, SpawnReason.SPAWN_ITEM_USE);
```

The prefixes of attributes in `EntityAttributes`, like `GENERIC`, have been dropped. This matches a change in the identifiers of attributes:

```
- public static final RegistryEntry<EntityAttribute> GENERIC_ATTACK_KNOCKBACK = register(
-    "generic.attack_knockback",
-    new ClampedEntityAttribute("attribute.name.generic.attack_knockback", 0, 0, 5)
);
+ public static final RegistryEntry<EntityAttribute> ATTACK_KNOCKBACK = register(
    "attack_knockback",
    new ClampedEntityAttribute("attribute.name.attack_knockback", 0, 0, 5)
);
```

#### Data generation

A recipe provider must now override `getRecipeGenerator` instead of `generate`. The new method takes the registries and `exporter`, and returns a new instance of `RecipeGenerator` that actually generates the recipes.

```
- public void generate(RecipeExporter exporter) {
-    offerPlanksRecipe2(exporter, SIMPLE_BLOCK, ItemTags.ACACIA_LOGS, 1);
+ protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registries, RecipeExporter exporter) {
+    return new RecipeGenerator(registries, exporter) {
+        @Override
+        public void generate() {
+            offerPlanksRecipe2(SIMPLE_BLOCK, ItemTags.ACACIA_LOGS, 1);
```

#### ActionResult

Previously, action results have been represented in several ways, including the `ActionResult`, `ItemActionResult`, and `TypedActionResult` classes. In Minecraft 1.21.2, these classes have been merged into a single `ActionResult` class, which provides direct replacements for the previous methods for all three classes:

|Old|New|
|---|---|
|`ActionResult.success(world.isClient())`|`ActionResult.SUCCESS`|
|`TypedActionResult.pass(stack)`|`ActionResult.PASS`|
|`TypedActionResult.fail(stack)`|`ActionResult.FAIL`|
|`TypedActionResult.success(stack, world.isClient())`|`ActionResult.SUCCESS`|
|`TypedActionResult.consume(stack)`|`ActionResult.CONSUME`|

If an action replaces the hand stack with another instance of `ItemStack`, then it should be marked with the `ActionResult#withNewHandStack` method. For example, an `Item#use` implementation that replaces the hand stack might be:

```
public ActionResult use(World world, PlayerEntity user, Hand hand) {
    ItemStack stack = user.getStackInHand(hand);
    
    if (stack.getCount() > 16) {
        ItemStack newStack = new ItemStack(Items.BLAZE_ROD, 16);
        return ActionResult.SUCCESS.withNewHandStack(newStack);
    }
    
    return ActionResult.PASS;
}
```

On the other hand, if the `ItemStack` instance for the hand stack is the same as the one provided to an interaction method such as `Item#use`, the `ActionResult#withNewHandStack` method should not be called.

This change affects all places where `TypedActionResult` and `ItemActionResult` were previously used in Minecraft’s code. In Fabric API, the `UseItemCallback` event in the Events Interaction module now returns `ActionResult` instead of `TypedActionResult<ItemStack>`.

#### Item components

Because elytra behavior is now controlled by a new item component, `minecraft:glider`, `FabricElytraItem` was removed. Add the component to your elytra item instead.

#### Block entities

In Minecraft 1.21.1 (released in August), a change was made that required mods to add all supported blocks to block entities. For example, a mod with a new sign block must add the block to `BlockEntityType#SIGN`:

```
BlockEntityType.SIGN.addSupportedBlock(ModBlocks.TEAL_SIGN);
```

In Minecraft 1.21.2, block entity types are no longer constructed using builders. Therefore, `FabricBlockEntityType.Builder` was removed, while `FabricBlockEntityTypeBuilder` is no longer deprecated.

#### Registries

`Registry` now implements `RegistryEntryLookup`. This resulted in several name changes. To query a `RegistryEntry` from `RegistryKey`, use `getOptional` or `getOrThrow`. (Same applies to `RegistryEntryList` from `TagKey`.) If you want to query the registry value, use `getValueOrThrow`. See the table below for all changes.

|Old|New|
|---|---|
|`getEntry`|`getOptional`|
|`entryOf`|`getOrThrow`|
|`getOrThrow`|`getValueOrThrow`|
|`getOrEmpty`|`getOptionalValue`|
|`getEntryList`|`getOptional`|

Similarly, `DynamicRegistryManager#get` and `RegistryWrapper.WrapperLookup#getWrapperOrThrow` has been renamed to `getOrThrow`. `getOptionalWrapper` was renamed tp `getOptional`.

#### Rendering

##### Entity rendering

Entity rendering has received a large refactor that decouples the `Entity` instance from its respective rendering calls. `EntityRenderer` now has an additional type parameter, `S extends EntityRenderState`, which represents a ‘render state’, which is a mutable class containing only the parameters of the entity that are used in rendering.

Previously, renderer methods accessed the entity instance directly. However, accessing entity information now happens in three steps.

First, the `EntityRenderer#createRenderState` method is called to construct an instance of `S` for the given entity with placeholder values:

```
public T createRenderState() {
    return new T();
}
```

Next, the `EntityRenderer#updateRenderState` method performs the operation of copying information from the entity to its render state:

```
public void updateRenderState(T entity, S state, float tickDelta) {
    super.updateRenderState(entity, state, tickDelta);

    // Example: entity has an 'is saddled' field
    state.isSaddled = entity.isSaddled();
}
```

Finally, the `EntityRenderer#render` method (as well as other renderer methods) accesses information from the entity’s render state:

```
public void render(S state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
    super.render(state, matrices, vertexConsumers, light);
    
    if (state.isSaddled) {
        // Render the saddle
    }
}
```

##### Shaders

In Fabric Rendering API, `CoreShaderRegistrationCallback` was removed. Vanilla resource pack now allows loading modded core shaders.

##### Other changes

`WorldRenderer` methods for rendering certain vertices (like a box) were transferred to `VertexRendering`.

#### Recipes

The recipe system was reworked. To summarize:

- Recipes are now identified using `RegistryKey<? extends Recipe>`, not `Identifier`.
- `Recipe` and the recipe ID is now server-side only. It cannot be accessed from the client.
- Instead, clients receive `RecipeDisplayEntry`. It contains all information needed to display and run the recipe book. Recipes are identified using an integer (`NetworkRecipeId`) on the client. A recipe can have multiple display entries.
- `IngredientPlacement` controls the placement of ingredients on the crafting table, while `RecipeMatcher` handles recipe matching.

`Ingredient` is now internally a list of items, not item stacks.

In Fabric API, `CustomIngredientSerializer#getCodec`: `allowEmpty` param removed, empty now always disallowed.

#### Biome

Water cave carvers (which, before 1.18, were seen in seabeds) were removed, along with `GenerationStep.Carver`.

In Fabric Biome API, biome modification methods like `addCarver` and `removeCarver` no longer takes `GenerationStep.Carver` argument.

#### Profiler

The shared profiler is now accessed using `Profilers#get`.

```
- world.getProfiler().push("tinyPotato");
+ Profilers.get().push("tinyPotato");
```

#### Loot tables

`LootTables#EMPTY` was removed. The game uses `Optional` to mark the lack of loot tables.

Some loot context-related logics are now shared with recipes, resulting in name changes. `LootContextParameter` is now `ContextParameter`, and `LootContextType` is now `ContextType`.

#### Yarn renames

_This section only affects those using the Yarn mapping._

As part of regular Yarn maintainace, the following changes were made. These should be a simple replacement:

- `ModelTransformationMode` is transferred to `net.minecraft.item`.
- `RealmsLoadingWidget` is renamed to `LoadingWidget` and transferred to `client.gui.widget`.
- Several `Brain` and `Task`-related names: see [the pull request](https://github.com/FabricMC/yarn/pull/3992) for details.
- `registryLookupFuture` is renamed to `registriesFuture` in several locations.
- `Codecs#NONNEGATIVE_INT` is renamed to `NON_NEGATIVE_INT`.
- `QUATERNIONF` and `VECTOR3F` are renamed to `QUATERNION_F` and `VECTOR_3F`.
- `Screen#initTabNavigation` is renamed to `refreshWidgetPositions`.
- `ComponentMapImpl` is renamed to `MergedComponentMap`.
- `ContainerComponentModifier#create` is renamed to `apply`.
- `ItemStack#encode` is renamed to `toNbt`.

## Updating to 1.21.4

Minecraft 1.21.4, the Garden Awakens drop, releases December 3rd. Like with other updates, this drop contains some significant changes affecting mod makers.

As always, **we ask all players to be patient, and give mod developers time to update to this new version.** We kindly ask everyone not to pester them. **We also recommend all players make backups of their worlds.**

Here is a list of several major modder-facing changes in this version. Note that all code references are using Yarn mappings; modders using alternative mappings may need to use different names.

### Fabric changes

Developers should use Loom 1.9 (at the time of writing) to develop mods for Minecraft 1.21.4. Players should install the latest stable version of Fabric Loader (currently 0.16.9).

#### Deprecations and removals

The following deprecated module was removed: `fabric-rendering-v0`. `FabricModelPredicateProviderRegistry`, which was previously deprecated, was also removed.

`BuiltinItemRenderer` and `BuiltinItemRendererRegistry` from the Rendering v1 module were removed and have been replaced by a transitive access widener to `SpecialModelTypes.ID_MAPPER`.

`AttachmentRegistry#builder` was deprecated in favor of `create` methods. The old method is inconvenient, as it requires explicit type parameters. (See the “Data Attachment Syncing” section for an example.)

`BlockPickInteractionAware` was removed, the new pick item events should be used instead.

#### Breaking change

`CustomIngredient#getMatchingStacks` must now return a stream, not a list.

#### New Fabric API changes

With the help of many contributors, Fabric API has received some new features since the last update blog post:

- Convention Tags: Add more tags (JT122406, TelepathicGrunt, IThundxr)
- Data Attachments: Registration enhancements (forgetmenot13579)
- Data Attachments: Sync API (Syst3ms)
- Lifecycle Events: Add `ServerChunkEvents.Generate` (jpenilla)
- Lifecycle Events: Add `AFTER_CLIENT_WORLD_CHANGE` (fishshi)
- Transfer API: Add support for Item-containing Items (BasiqueEvangelist)

In addition, a longstanding bug that caused language files to not load after overriding `assets/minecraft/lang/en_us.json` in a dedicated server mod was fixed.

##### Data Attachment Syncing

Fabric API can now sync data attachments. To make a syncable attachment, call `syncWith` inside the builder. The passed packet codec is used to serialize the attached data. Here is how a thirst attachment would look like:

```
public static final AttachmentType<Integer> THIRST = AttachmentRegistry.create(
    Identifier.of("modid", "thirst"),
    builder -> builder 
        .initializer(() -> 20) // start with a default value like hunger
        .persistent(Codec.INT) // persist across restarts
        .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.targetOnly()) // only the player's own client needs the value for rendering
    );
```

The sync predicate (`AttachmentSyncPredicate`) controls who gets the synced data. For example, global data can be synced using `all()`. For more granular control, you can also pass a custom predicate.

##### Pick item events

Minecraft 1.21.4 moves the ‘Pick Block’ functionality from the client to the logical server. As such, the existing client events have been replaced with new, server-side ones. This replaces `ClientPickBlockApplyCallback`, `ClientPickBlockCallback`, and `ClientPickBlockGatherCallback`. This change also applies to entities (`EntityPickInteractionAware`).

The new events are `PlayerPickItemEvents#BLOCK` for picking a block and `PlayerPickItemEvents#ENTITY` for picking an entity(‘s spawn egg). Return `ItemStack.EMPTY` to stop the picking, and return `null` to use the default behavior.

```
PlayerPickItemEvents.BLOCK.register((player, pos, state, requestIncludeData) -> {
    if (state.isIn(MyTags.NOT_PICKABLE)) return ItemStack.EMPTY;

    return null; // use default behavior
})
```

`requestIncludeData` is `true` if the client requests NBT to be included in the returned item stack (by holding `Ctrl` while picking block). This parameter is also available to the entity pick item event, even though vanilla does not use this functionality (spawn eggs do not include the picked entity’s NBT).

Note that this only checks if the client is asking NBT data, and does not check game mode or permission level. In vanilla, NBT data is only included for Creative mode players.

##### Client Data Generation

Starting with Minecraft 1.21.4, Mojang has moved a number of data generation classes to the client. Loom 1.9 adds a new option that allows you to run your data generation against the client. See the following example if you are using the `fabricApi` utility to set up data generation:

```
  fabricApi {
-     configureDataGeneration()
+     configureDataGeneration {
+       client = true
+     }
  }
```

If you are using the `createSourceSet` option, your `datagen` source set will now have access to Minecraft’s client-only classes and classes in your `client` source set. Other than updating the data generators themselves, no other changes will be necessary.

If you are using the `splitEnvironmentSourceSets` option but not the `createSourceSet` option, you should move your `DataGeneratorEntrypoint` implementation from the `main` source set to the `client` source set.

##### Model Loading API

`ModelModifier` events and callbacks have been split; there is now one set for static models and one set for block models. This was necessary because static models use `UnbakedModel` and are baked with settings while block models use `GroupableModel` (which no longer extends `UnbakedModel`) and are not baked with settings. It also allowed cleaning up the identifier getters and providing the `BlockState` directly to `ModelModifier.OnLoadBlock`.

However, all `BeforeBake` and `AfterBake` events were removed. This is because their behavior can now be achieved using the `OnLoad` events, by wrapping the given model and overriding the `bake` method to replace the unbaked model passed to `super.bake` (to replicate `BeforeBake`) or replace the result of `super.bake` (to replicate `AfterBake`). This is possible because 1.21.4 made it so the parent of a `JsonUnbakedModel` no longer has to be another `JsonUnbakedModel`. To make this easier, the utility classes `WrapperUnbakedModel` and `WrapperGroupableModel` were added, which forward all method calls to a `wrapper` field. There were also some minor fundamental issues with `BeforeBake` and `AfterBake` events, which prompted their removal.

`ModelModifier.OnLoad` can now accept a `null` model, which is a model that was requested during resolution but does not have a corresponding JSON file. With this change, `ModelResolver` was removed as its behavior could now be achieved with `ModelModifier.OnLoad` with `OVERRIDE_PHASE`.

`DelegatingUnbakedModel` was removed as `GroupableModel` no longer extends `UnbakedModel`, so `BlockModelResolver`s can no longer use it, which was its original purpose.

`textureGetter` was removed from `ModelModifier` callback contexts as it is now accessible through `Baker#getSpriteGetter`.

FRAPI’s `WrapperBakedModel` was moved to Model Loading API and was renamed to `UnwrappableBakedModel` to avoid conflicts with vanilla’s new `WrapperBakedModel`. `UnwrappableBakedModel` is also implemented and interface injected on vanilla’s `WrapperBakedModel`. A new static `UnwrappableBakedModel#unwrap` method was added which accepts a `Predicate` saying when to stop unwrapping. See the documentation for more details.

### Minecraft changes

#### Block Models

Previously, block entities could choose whether they rendered their block model in addition to any custom block entity rendering. This method has been removed, as now all block entities render their block models:

```
- @Override
- protected BlockRenderType getRenderType(BlockState state) {
-     return BlockRenderType.ENTITYBLOCK_ANIMATED;
- }
```

While block models were already used to provide particle textures in these cases, mods should still ensure their block entities’ block models are correct.

#### Item Models

Similar to the blockstate definition json files found in `assets/<namespace>/blockstates`, items now utilize new item model definition json files in `assets/<namespace>/items` to determine which models to use.

These can be quite simple…

```
{
  "model": {
    "type": "minecraft:model",
    "model": "modid:item/my_item"
  }
}
```

or more complex…

```
{
  "model": {
    "type": "minecraft:condition",
    "property": "minecraft:fishing_rod/cast",
    "on_false": {
      "type": "minecraft:model",
      "model": "modid:item/netherite_fishing_rod"
    },
    "on_true": {
      "type": "minecraft:model",
      "model": "modid:item/netherite_fishing_rod_cast"
    }
  }
}
```

utilising conditions and other logic to determine which model to use.

They also control applying color tints to item textures.

```
{
  "model": {
    "type": "minecraft:model",
    "model": "minecraft:item/template_spawn_egg",
    "tints": [
      {
        "type": "minecraft:constant",
        "value": 6925483
      },
      {
        "type": "minecraft:constant",
        "value": 12238402
      }
    ]
  }
}
```

These tints can be constant or based on other factors such as potion contents, map colors, etc.

As a result of these files being introduced, blocks whose item models previously simply referred to their block model no longer require a separate item model file, as the block model is referred to in the model definition file instead. For example,

```
- {
-  "parent": "modid:block/maple_planks"
- }
```

at `assets/modid/models/item/maple_planks.json` can be removed, in favor of

```
+ {
+  "model": {
+    "type": "minecraft:model",
+    "model": "modid:block/maple_planks"
+  }
+ }
```

in `assets/modid/items/maple_planks.json`.

Additionally, `ItemColors` and related APIs for tinting items have been removed, as item color tints are now controlled by these files.

For further information, these jsons are well documented on [the vanilla Minecraft wiki here](https://minecraft.wiki/w/Items_model_definition) and all the ones used by vanilla items can be easily found in the game files.

For modders using data generation, these can be generated much like any other JSON asset.

#### Miscellaneous

- Resource Metadata files now use Codec serializers
- Equipment assets were moved from `assets/<namespace>/models/equipment` to `assets/<namespace>/equipment`
- The `minecraft:tall_flowers` block tag was removed
- The `minecraft:flowers`, `minecraft:tall_flowers`, and `minecraft:trim_templates` item tags were removed
- The `minecraft:herobrine` entity was removed
- Trim Material jsons no longer specify an item model index, as trimmed items now use separate item models for each material
- Textures for item outlines shown in empty slots in certain GUIs are now located in `assets/<namespace>/textures/gui/sprites/container/slot`

### Yarn changes

There have been many changes to Yarn mappings to reflect refactors to the vanilla game, fix issues with the mappings, or to otherwise improve them.

Some notable examples include:

- The `net/minecraft/data/client` package got moved to `net/minecraft/client/data`, to reflect the classes now being client-only
- To match this, the `net/minecraft/data/server` package was removed and its contents moved down to `net/minecraft/data`
- All references to `xp` got changed to `experience`
- `BlockStateModelGenerator.TintType` was changed to `BlockStateModelGenerator.CrossType`
- `CherryLeavesBlock` was changed to `ParticleLeavesBlock`
- `CherryLeavesParticle` was changed to `LeavesParticle`
- `LichenGrower` was changed to `MultifaceGrower`
- `EntityModelLoader` was changed to `LoadedEntityModels`
- `CocoaBeansTreeDecorator` was changed to `CocoaTreeDecorator`
- `BakedQuad#colorIndex` was changed to `tintIndex`

## Updating to 1.21.5

A new version of Minecraft is coming soon with some changes that affect most mod makers. As always, **we ask all players to be patient, and give mod developers time to update to this new version.** We kindly ask everyone not to pester them. **We also recommend all players make backups of their worlds.**

Here is a list of several major modder-facing changes in this version. Note that all code references are using Yarn mappings; modders using alternative mappings may need to use different names.

### Fabric changes

Developers should use Loom 1.10 (at the time of writing) to develop mods for Minecraft 1.21.5. Players should install the latest stable version of Fabric Loader (currently 0.16.10).

#### Loom 1.10

Loom 1.10 requires Gradle 8.12, and comes with performance improvements and enhanced testing setup support. You can checkout the new Loom documentation [here](https://docs.fabricmc.net/develop/loom/).

#### Deprecations and removals

In Content Registries, `VillagerInteractionsRegistries#registerGiftLootTable` overload that takes an `Identifier` was removed. This method was previously deprecated.

In Object Builder, two deprecated classes, namely `VillagerProfessionBuilder` and `VillagerTypeHelper`, have been removed. Use the `VillagerProfession` constructor and `VillagerType#create` instead. `TradeOfferHelper#refreshOffers`, which had been deprecated and did nothing, was also removed.

`HudRenderCallback` has been deprecated in favor of newly added `HudLayerRegistrationCallback`.

#### Breaking changes

When a mod creates a new dynamic registry, the data pack JSON files for the registry must now be placed inside namespaced directories. For example, if the new registry is `example:potato_variant`, the file for variant `test:tiny` will be placed in `data/test/example/potato_variant/tiny.json`.

`BiomeModificationContext#addSpawn` has a new parameter, `weight`. This was previously part of `SpawnEntry`.

`TradeOfferHelper` now takes a `RegistryKey` of the profession instead of `VillagerProfession`. Wandering trader trades must now be added via the builder, which was previously used for the Rebalance experiment. (The rebalanced trades are now used in all worlds.)

#### New Fabric API changes

With the help of many contributors, Fabric API has received some new features since the last update blog post:

- New module: Client Game Test API, which can be used to automatically test client rendering and GUI (Earthcomputer)
- New module: Fabric Tag API, which currently handles tag aliases (Juxxel)
- Convention Tags: Sync remaining `c` tags with NeoForge (TelepathicGrunt)
- Convention Tags: Add `c:flowers`, `c:flowers/tall`, and `c:flowers/small` block and item tags (TelepathicGrunt)
- Convention Tags: Add `TagKey` for `c:tools/wrench` (TelepathicGrunt)
- Convention Tags: Convention Drink Tags (TheDeathlyCow)
- Convention Tags: Add Pumpkin Block and Item Tags (JT122406)
- Data Generation: Add vararg helper methods for multi-tag support in `FabricTagBuilder` (Starexify)
- Data Generation: Add `FabricEntityLootTableProvider` (Antikyth)
- Item API: Add a method for overriding modelId in item settings (Patbox)
- Item API: Add `contains` method to `FabricComponentMapBuilder` (TheDeathlyCow)
- Item Group API: Change Creative Buttons Texture (matthewperiut)
- Item Group API: Use page up/down to change creative inventory pages (modmuss50)
- Model Loading API: Allow retrieving model loading plugins (PepperCode1)
- Networking API: Add `ServerPlayNetworking.reconfigure` (modmuss50)
- Object Builder: Allow setting `canPotentiallyExecuteCommands` in builders (PepperCode1)
- Recipe API: Add `getAllMatches` and `getAllOfType` methods to `ServerRecipeManager` (Patbox)
- Registry Sync: Add `RegistryAttribute#OPTIONAL` that can be used to not disconnect clients lacking an entire registry; note that optional registry values are still unsupported (modmuss50)
- Registry Sync: Registry aliasing (Syst3ms)
- Rendering: Add `SpecialBlockRendererRegistry` (PepperCode1)
- Rendering: Add HUD Render Events (kevinthegreat1)
- Resource Loader: Implement builtin mod resource/data pack sorting (Apollo)

#### Tag and registry aliases

The new tag and registry alias APIs allow for mods to seemlessly migrate their tags and registry aliases to new names. Thanks to Juuxel and Syst3ms respectively for implementing these new APIs.

```
Registries.BLOCK.addAlias(Identifier.of("my_mod", "old"), Registries.BLOCK.get(Identifier.of("my_mod", "new")));
```

Registry aliases are really simple; with the code above, any access to the old ID in the registry is redirected to the new ID. This allows worlds to be upgraded to use the new ID; for example, a block with the old ID becomes the one with the new ID.

Tag aliases are defined in data packs like tags. For example, a block tag alias group for fences would be located at `data/my_mod/fabric/tag_aliases/block/fences.json` with the contents:

```
{
  "tags": [
    "minecraft:fences",
    "c:fences"
  ]
}
```

#### Client GameTest

For a long time, Fabric has had an internal client test framework that was used for testing that Fabric API was working correctly on the client. In a series of PRs, Earthcomputer has worked to expand and expose this framework to mod developers. The new experimental API has the following features:

- World creation API, for specifying options used to generate the test world.
- Screenshot API, with support for comparing against golden images.
- Input API, to simulate a user interacting with the game.
- Advanced threading setup, to make the tests more repoducible.
- Network synchronization, to ensure packets are handled consistently.
- Herobrine removal, to bring peace to the Kingdom of Tiny Potato.

For more information checkout the full documentation [here](https://maven.fabricmc.net/docs/fabric-api-0.119.2+1.21.5/net/fabricmc/fabric/api/client/gametest/v1/package-summary.html).

#### HUD Render Events

Fabric API 0.116.0 added `HudLayerRegistrationCallback` event, providing full control over the HUD rendering process. The new API assigns idenftifiers to each layer that can be used to specify where things will be rendered. The new API also allows replacing or removing existing layers. A big thanks to kevinthegreat1 and many other people for making this happen!

### Minecraft changes

#### NBT

Significant changes to NBT handling code have been made.

`NbtCompound` methods now return `Optional` instead of the value. If the key is not in the compound or if the value is not of the correct type, an empty optional is now returned instead of that type’s default value.

```
- int value = nbt.getInt("value");
+ Optional<Integer> value = nbt.getInt("value"); // not OptionalInt
```

For primitives (numbers and strings), instead of checking for the existence of a key and handling a fallback, the fallback can now be passed to `get` methods:

```
- int value;
- if (nbt.contains("value", NbtElement.NUMBER_TYPE)) {
-   value = nbt.getInt("value");
- } else {
-   value = 1000;
- }
+ int value = nbt.getInt("value", 1000);
```

Also note that `NbtElement#NUMBER_TYPE` and type-aware `contains` have been removed. There is no fallback method for `getIntArray`, `getLongArray`, and `getByteArray`. (Use `Optional#orElse`.)

For `getCompound` and `getList` methods, methods with the previous behavior (returning empty objects) are provided with `OrEmpty` suffix.

```
- NbtCompound config = nbt.getCompound("config");
+ NbtCompound config = nbt.getCompoundOrEmpty("config");
```

Finally, for those who want to encode/decode codec-based values (such as `Identifier`) stored in a `NbtCompound` field, new methods simplify the process:

```java=
NbtCompound nbt = new NbtCompound();
nbt.put("Id", Identifier.CODEC, id);
// for reading
Optional<Identifier> id = nbt.get("Id", Identifier.CODEC);
```

And if there is a codec for the whole object:

```java=
NbtCompound nbt = new NbtCompound();
// have to use the RegistryOps since an item is a registry entry
nbt.copyFromCodec(ItemStack.MAP_CODEC, wrapperLookup.getOps(NbtOps.INSTANCE), stack);
// for reading
Optional<ItemStack> stack = nbt.decode(ItemStack.MAP_CODEC, wrapperLookup.getOps(NbtOps.INSTANCE));
```

Typed arrays (`ByteArray`, `IntArray`, and `LongArray`) are no longer `List`s. They can be converted to a list using `.stream().toList()`, but this is usually not necessary. `NbtList` can now contain values of differing types, but this should be a transparent change unless you work with binary data.

#### GameTest

In Minecraft [25w03a](https://www.minecraft.net/en-us/article/minecraft-snapshot-25w03a), Mojang totally refactored the vanilla testing framework, exposing it to datapack developers. Unfortunately, the new API is a little bit cumbersome for mod developers. Fabric API now provides its own `@GameTest` annotation that functions similarly to the old one. The options in the new Fabric-provided `@GameTest` annotation directly map the vanilla data-driven options, removing the need to have a JSON file for each test function. Data driven tests will still work if you wish to use the vanilla system.

#### Miscellaneous

- `DataPool` has been replaced with `Pool`.
- `AbstractBlock#onStateReplaced` has been significantly changed, the state provided is the old state and it now runs after block entities get removed. Block entities should use `BlockEntity#onBlockReplaced` instead.

## Updating to 1.21.8

A new version of Minecraft is coming soon with some changes that affect most mod makers. As always, **we ask all players to be patient, and give mod developers time to update to this new version.** We kindly ask everyone not to pester them. **We also recommend all players make backups of their worlds.**

Here is a list of several major modder-facing changes in this version. Note that all code references are using Yarn mappings; modders using alternative mappings may need to use different names.

### Fabric changes

Developers should use Loom 1.10 (at the time of writing) to develop mods for Minecraft 1.21.6. Players should install the latest stable version of Fabric Loader (currently 0.16.14).

#### Deprecations and removals

The following previously deprecated modules have been removed ([#4651](https://github.com/FabricMC/fabric/pull/4651)):

- `fabric-command-api-v1`
- `fabric-commands-v0` (Deprecated almost 5 years ago!)
- `fabric-keybindings-v0`
- `fabric-rendering-data-attachment-v1`

The following modules have been merged into other modules for simplicity:

- `fabric-client-tags-api-v1` was merged into `fabric-tag-api-v1` ([#4647](https://github.com/FabricMC/fabric/pull/4647))
- `fabric-blockrenderlayer-v1` was merged into `fabric-rendering-v1` ([#4675](https://github.com/FabricMC/fabric/pull/4675))

The tag API changes are technically breaking for some developers who explicitly depend on these modules. The removal and merging of these modules has been done to help improve peformance when setting up a new development environment.

The Fabric Rendering API previously provided a Material API, to allow modders more control of the way their models rendered. This has been removed.

> Materials were removed … because they were deemed to be an unnecessary part of the API design, and the breaking change induced by changes in 1.21.6 was related to materials, which made this the perfect time to remove them - PepperCode1

See ([#4675](https://github.com/FabricMC/fabric/pull/4675)) for more info.

In addition to being relocated, the `BlockRenderLayerMap` API was also updated to be more consistent out current API style:

```
- import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
+ import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
 ...
- BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.MY_EPIC_BLOCK, BlockRenderLayer.CUTOUT)
+ BlockRenderLayerMap.putBlock(ModBlocks.MY_EPIC_BLOCK, BlockRenderLayer.CUTOUT)
```

See ([#4664](https://github.com/FabricMC/fabric/pull/4664)) for more info.

#### Breaking changes

Fabric’s brand new HUD API had to be totally rewritten in 1.21.6. The new `HudElementRegistry` provides all of the functionality provided by the old API. The following basic example shows how you can draw text after all of the vanilla HUD layers:

```
HudElementRegistry.addLast(Identifier.of("example", "hud"), (context, tickCounter) -> {
	context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "This is an example", 10, 10, Colors.WHITE);
});
```

If you wish to render your custom hud element before the vanilla chat you can do the following:

```
HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.of("example", "ud"), (context, tickCounter) -> {
	// ...
});
```

#### New Fabric API features

Since `Item#appendTooltip` has become deprecated, Fabric API now provides the `ComponentTooltipAppenderRegistry`. This registry provides `addAfter`, `addBefore`, `addFirst`, and `addLast` to allow you to position your tooltips relative to vanilla and other mods.

```
record MyAmazingComponent() implements TooltipAppender {
	@Override
	public void appendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType type, ComponentsAccess components) {
		textConsumer.accept(Text.literal("Amazingness Awaits!"));
	}
}
ComponentType<MyAmazingComponent> myAmazingComponentComponentType = /*...*/;
ComponentTooltipAppenderRegistry.addAfter(
	DataComponentTypes.DAMAGE,
	myAmazingComponentComponentType
);
```

Any `ItemStack` that has your component applied will call your component’s `appendTooltip` method, allowing you to append as you wish. ([#4587](https://github.com/FabricMC/fabric/pull/4587))

The LootTable API has been expanded to make certain extreme usages more convenient. The `LootTableEvents.MODIFY_DROPS` event allows modders to customize the collective output of `LootTable`s. The `LootTableEvents.MODIFY` event should still be preferred when possible, for mod compatibility reasons. This event may also recurse if you generate loot from within a listener. ([#4643](https://github.com/FabricMC/fabric/pull/4643))

```
var matchGetter = ServerRecipeManager.createCachedMatchGetter(RecipeType.SMELTING);

// smelt any smeltable drops from blocks broken with a diamond pickaxe
LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
	if (!context.hasParameter(LootContextParameters.TOOL)) return;
	if (!context.hasParameter(LootContextParameters.BLOCK_STATE)) return;
	ItemStack tool = context.get(LootContextParameters.TOOL);
	if (!tool.isOf(Items.DIAMOND_PICKAXE)) return;
	var world = context.getWorld();
	var lookup = world.getRegistryManager();
	drops.replaceAll(drop ->
		matchGetter.getFirstMatch(new SingleStackRecipeInput(drop), world)
			.map(RecipeEntry::value)
			.map(recipe -> recipe.craft(input, lookup))
			.orElse(drop)
	);
});
```

Continuing with our conventional tag API, we added new biome tags, allowing modders to differentiate biomes based on their primary wood type. The `ServerChunkEvents.CHUNK_LEVEL_TYPE_CHANGE` event was added to allow more control over the timing of chunk events. This event fires for changes in chunk loading level, to react to changes not previously possible without mixins. ([#4541](https://github.com/FabricMC/fabric/pull/4541))

An event was added for attachment changes, allowing reaction to an attachment value changing. This event can be recursive in nature, as if you set an attachment value from within a listener, the event will be invoked again. Modders should use proper recursion techniques to prevent infinite recursion. ([#4606](https://github.com/FabricMC/fabric/pull/4606))

Two more events were added for players joining and leaving the game:

```
AttachmentType<Instant> JOINED_TIME = /*...*/;
ServerPlayerEvents.JOIN.register(player -> {
	// runs on the main thread, no need to use player.getServer().execute(() -> ...);
	player.setAttached(JOINED_TIME, Instant.now());
});
```

```
List<ServerPlayerEntity> activePlayers = /*...*/;
ServerPlayerEvents.LEAVE.register(activePlayers::remove);
```

These events are designed for initializing and de-initializing state related to players, and run along vanilla code with the same purpose on the main thread, unlike the current events. ([#4642](https://github.com/FabricMC/fabric/pull/4642))

The `FabricSoundsProvider` class was added to allow convenient creation of `sounds.json` from within datagen. ([#4560](https://github.com/FabricMC/fabric/pull/4560))

The Client Game Test API has been tweaked to support filtering tests run by model, allowing more precise and efficient testing. ([#4597](https://github.com/FabricMC/fabric/pull/4597))

The Model Loading API now supports registering extra unbound models ([#4565](https://github.com/FabricMC/fabric/pull/4565))

```
// A ModelKey is a unique identifier for a model you want to bake.
public static final ModelKey<BlockStateModel> HALF_RED_SAND_MODEL_KEY = ModelKey.create();
public static final Identifier HALF_RED_SAND_MODEL_ID = id("half_red_sand");

public static void init() {
	ModelLoadingPlugin.register(pluginContext -> {
		pluginContext.addModel(HALF_RED_SAND_MODEL_KEY, HALF_RED_SAND_MODEL_ID, (model, baker) -> {
			ModelTextures textures = model.getTextures();
			return new SimpleBlockStateModel(new GeometryBakedModel(
				model.bakeGeometry(textures, baker, ModelRotation.X0_Y0),
				model.getAmbientOcclusion(),
				model.getParticleTexture(textures, baker)
			));
		});
	})
}

public static BlockStateModel getModel() {
	return MinecraftClient.getInstance().getBakedModelManager().getModel(HALF_RED_SAND_MODEL_KEY);
}
```

`FabricTrackedDataRegistry` has been added to allow registering tracked data handlers for entities. This removes conflicts between mods registering tracked data handlers and ensures that the order is consistent between the client and server. If you previously used the vanilla API the following 1 line change is all you need to take advantage of this new API:

```
- TrackedDataHandlerRegistry.register(TRACKED_DATA_HANDLER);
+ FabricTrackedDataRegistry.registerHandler(TRACKED_DATA_HANDLER_ID, TRACKED_DATA_HANDLER);
```

#### Bug Fixes

Thanks to the diligent developers and players, many bugs in Fabric API were reported and patched during this update cycle. See [The Fabric Github](https://github.com/FabricMC/fabric/pulls?q=is%3Apr+is%3Aclosed+label%3Abug) for more info.

### Minecraft changes

#### Rendering

Mojang is currently working on separating Minecraft’s rendering pipeline into two stages:

1. The extraction stage, where all renderable data is seperated from the game
2. The render phase, where the previously extracted data is rendered.

This process began in 1.21.2, and is still incomplete as of this update. Chunk, GUI and HUD rendering have all been converted to use the new separate rendering style. The ultimate goal of this separation is to enable the game to render one frame while the next is being extracted.

Many methods in `RenderSystem` have been removed without direct replacement. In most cases, there isn’t a one-to-one translation from the old code to the new, but the same capabilities exist by combining the new `RenderPipeline`s with `RenderLayer`s.

#### NBT

`BlockEntity`s now abstract saving to NBT through `ReadView`s and `WriteView`s. These views are responsible for storing errors from encoding / decoding, and keeping track of registries throughout the serialization process. You can read from a `ReadView` using the `read` method, passing in a `Codec` for the desired type. Likewise, you can write to a `WriteView` by using the `put` method, passing in a `Codec` for the type, and the value in question. there are also methods for primitives, under `get(Int, Short, Boolean, ...)` and `put(Int, Short, Boolean, ...)`. The View also provides methods for working with lists, nullable types, and nested objects.

```
class BE extends BlockEntity {
	private int anInt;
	private String aString;
	private Extra extra;
	// Ctor excluded for brevity
	@Override
	public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
		return createNbt(registries); // createNbt takes care of adapting to / from WriteView
	}

	@Override
	protected void writeData(WriteView view) {
		super.writeData(view);
		view.putNullable("extra", Extra.CODEC, this.extra);
		if (aString != null) // putString will eventually throw if we pass null
			view.putString("aString", aString);
		view.putInt("anInt", anInt);
	}

	@Override
	protected void readData(ReadView view) {
		super.readData(view);
		view.read("extra", Extra.CODEC).ifPresent(extra -> this.extra = extra);
		view.getOptionalString("aString").ifPresent(aString -> this.aString = aString);
		view.getOptionalInt("anInt").ifPresent(anInt -> this.anInt = anInt);
	}

	record Extra(int i, int j) {
		public static final Codec<Extra> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.INT.fieldOf("i").forGetter(extra -> extra.i), Codec.INT.fieldOf("j").forGetter(extra -> extra.j)).apply(instance, Extra::new));
	}
}
```

#### Data Generation

- `getOrCreateTagBuilder` should be replaced with the new `valueLookupBuilder`.

## Updating to 1.21.10

A new version of Minecraft is coming soon with some changes that affect most mod makers. As always, **we ask all players to be patient, and give mod developers time to update to this new version.** We kindly ask everyone not to pester them. **We also recommend all players make backups of their worlds.**

Here is a list of several major modder-facing changes in this version. Note that all code references are using Yarn mappings; modders using alternative mappings may need to use different names.

### Fabric changes

Developers should use Loom 1.11 (at the time of writing) to develop mods for Minecraft 1.21.9. Players should install the latest stable version of Fabric Loader (currently 0.17.2).

#### Yarn Mappings

In this update, several mapping name changes were forced by changes in the vanilla class hierarchy. While a full diff may be found [here](https://github.com/FabricMC/yarn/compare/d9167974e1fc83c980cf3be9fb567b1ab67d1153..6f9f183d2908d05e67a89aa3558caf9fee207dc0), there is one major change affecting almost all mods: `Entity#getWorld` was renamed to `Entity#getEntityWorld`.

#### World Render Events

The current event suite for rendering in the world has been removed. A suitable replacement is planned asap, but not ready yet. In the meantime, please use mixins to implement what your mod needs.

#### Resource Loader API v1

A major rework of the resource loader API is present in the 1.21.9 version of Fabric API. This will make current functionality easier to acomplish, as well as opening doors for features like runtime resource generation in the future.

The first part of this rework has just landed with the focus being on `ResourceReloader`. Historically this API based itself on the `IdentifiableResourceReloadListener` interface which allowed `ResourceReloader` to both be identifiable and specify dependencies. However this API had limits, which prevented to run before another `ResourceReloader` or was difficult to use in multiloader environments. This has been fixed with this new iteration of the API.

From now on, `ResourceReloader` do not need to implement a Fabric-provided interface, instead they can be registered with an identifier directly:

```
- ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new CustomResourceReloader())
+ ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(Identifier.of("modid", "custom_resource_reloader"), new CustomResourceReloader());
```

Ordering is now specified akin to events:

```
ResourceLoader.get(ResourceType.SERVER_DATA).addReloaderOrdering(
    Identifier.of("other", "other_reloader_to_depend"), // Triggers first
    Identifier.of("modid", "custom_resource_reloader") // Triggers second
);
```

You can also order based on Vanilla reloaders thanks to `ResourceReloaderKeys`, which provides both per-resource-reloader an identifier, and two global keys: before and after vanilla.

Thanks to 1.21.9 Vanilla changes the way to register reloaders which need registry access has been simplified, instead of using a specialized registration method, now you can get registries and feature flags in `ResourceType.SERVER_DATA` reloaders via the shared state `Store`:

```
class DataReloader implements ResourceReloader {
    @Override
    public CompletableFuture<Void> reload(
        Store store,
        Executor prepareExecutor,
        Synchronizer reloadSynchronizer,
        Executor applyExecutor
    ) {
        RegistryWrapper.WrapperLookup registries = store.getOrThrow(ResourceLoader.RELOADER_REGISTRY_LOOKUP_KEY);
        FeatureSet featureSet = store.getOrThrow(ResourceLoader.RELOADER_FEATURE_SET_KEY);
        // Code
    }
}
```

This change also allows for `ResourceReloader`s to communicate data between them.

See [#4574](https://github.com/FabricMC/fabric/issues/4574) for more details about what else is planned and the current progress.

#### Enchantments

Transitive access wideners have been added for all of the utility methods in `EnchantmentHelper` and `Enchantment`. We hope that this will make it easier for developers to implement custom enchantment effect components. See [#4819](https://github.com/FabricMC/fabric/pull/4819) for more details.

#### Serialization

We have added a new module for utilities related to serialization. Currently, this module includes additional methods in `ReadView` and `WriteView`, and provides `Codec`s for some types that base vanilla doesn’t. Please feel free to suggest anything else that may be useful in this area. See [#4745](https://github.com/FabricMC/fabric/pull/4745) for more details.

#### Block Conversions

To closer align with vanilla code flow, `OxidizableBlocksRegistry` now supports registering a `CopperBlockSet` direclty. Simply call `OxidizableBlocksRegistry.registerCopperBlockSet(set)` to register. The prior methods for block pairs are still avalible for those that prefer them. See [#4807](https://github.com/FabricMC/fabric/pull/4807) for more details.

`StrippableBlockRegistry` now provides multiple overloads to satisfy all your stripping needs. As before, simple conversions can be registered by calling `StrippableBlockRegistry.register(Block, Block)`. You may also call `StrippableBlockRegistry.registerCopyState(Block, Block)` to register a stripping conversion that automatically copies all properties from the previous state, or `register(Block, Block, StrippingTransformer)` to have full control over the stripping process. See [#4829](https://github.com/FabricMC/fabric/pull/4829) for more details.

#### GUI Rendering

When using a custom `RenderPipeline` to render in the GUI, vanilla may assume that the `VertexFormat` being used is `QUADS`. Fabric now provides a way to override this behavior with `RenderPipeline.Builder.withUsePipelineDrawModeForGui`.

```
var pipeline = RenderPipeline.builder(
        snippet1, 
        snippet1
    )
    .withUsePipelineDrawModeForGui(true)
    .build();

// ...
fictionalRenderInGuiInNonGuads(pipeline); // will respect the VertexFormat set in the pipeline
```

See [#4824](https://github.com/FabricMC/fabric/pull/4824) for more details.

#### Mixin & MixinExtras

With version 0.17.0 of Fabric Loader, MixinExtras 5.0.0 and Fabric Mixin 0.16.3+mixin.0.8.7 are now bundled.

- MixinExtras 5.0.0 brings expressions, a new way to discribe mixins to java code in a syntax that mirrors the target, leading to mixins that are easier to maintain, more expressive, and potentially even more compatible with other mixins. See the [release notes](https://github.com/LlamaLad7/MixinExtras/releases/tag/0.5.0) and [wiki pages](https://github.com/LlamaLad7/MixinExtras/wiki/Expressions) for more details.
- Fabric Mixin 0.16.3 brings many bug fixes, along with scaffolding in mixin for potential widespread performance increases.

#### Screen Key Events

With many breaking changes from mojang regarding keybindings, we’ve taken the opportunity to improve our events. Most parameters in the event have been consolidated into a context object known as a `KeyInput`. The _afterMouseX_ style events now also take and return a `boolean` representing whether the event has been consumed. Returning `true` from these events will prevent further vanilla handling.

See [#4846](https://github.com/FabricMC/fabric/pull/4846/) and [#4620](https://github.com/FabricMC/fabric/pull/4620) for more details.

### Minecraft Changes

#### Rendering

Almost all world rendering has been reworked to group objects with similar rendering requirements together. Most places now use `OrderedRenderCommandQueue` to submit things to be drawn later, when all objects of a similar type have been rendered.

##### Block Entities

Block entities now use `OrderedRenderCommandQueue`. The following example shows rendering text on the block using the queue.

```
public class TestBlockEntityRenderer implements BlockEntityRenderer<TestBlockEntity, BlockEntityRenderState> {
    public TestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public BlockEntityRenderState createRenderState() {
        return new BlockEntityRenderState();
    }

    @Override
    public void render(BlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        queue.submitText(
                matrices,
                0,
                0,
                Text.literal("Hello, world!").asOrderedText(),
                false,
                TextRenderer.TextLayerType.NORMAL,
                state.lightmapCoordinates,
                Colors.WHITE,
                0,
                Colors.BLACK
        );
    }
}
```

```
public class TestBlockEntityRenderer implements BlockEntityRenderer<TestBlockEntity> {
    public TestBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(TestBlockEntity entity, float tickProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, Vec3d cameraPos) {
        MinecraftClient.getInstance().textRenderer.drawWithOutline(
                Text.literal("Hello, world!").asOrderedText(),
                0,
                0,
                Colors.WHITE,
                Colors.BLACK,
                matrices.peek().getPositionMatrix(),
                vertexConsumers,
                light
        );
    }
}
```

##### Particles

Particle rendering has been changed to use the same queue system as above. Many good examples for implementing custom rendering via the queue can be found in the vanilla particle classes.

##### Entities

Entity rendering has changed to use the same queue as shown above. Changes will be similar.

#### Keybinding Changes

Keybinding categories have become more structured. You could do the following before:

```
public class ModKeybindings {
  private static final KeyBinding RANDOM_KEYBIND = new KeyBinding(
    "key.test.random_keybind",
    InputUtil.Type.KEYSYM,
    InputUtil.UNKNOWN_KEY.getCode(),
    "key.category.test.main"
  );

  private static void tickKeybindings(MinecraftClient client) {
    while (RANDOM_KEYBIND.wasPressed()) {
      System.out.println("Random keybind pressed!");
    }
  }

  public static void init() {
    ClientTickEvents.END_CLIENT_TICK.register(ModKeybindings::tickKeybindings);
  }
}
```

Now you could do:

```
public class ModKeybindings {
  private static final KeyBinding.Category TEST_CATEGORY = KeyBinding.Category.create(Identifier.of("test", "main"));
  private static final KeyBinding RANDOM_KEYBIND = new KeyBinding(
    "key.test.random_keybind",
    InputUtil.Type.KEYSYM,
    InputUtil.UNKNOWN_KEY.getCode(),
    TEST_CATEGORY
  );
  // ...
}
```

Each category may only be registered once; Registering a category twice, or two categories with the same id will lead to an exception.

When Fabric API is installed, mod-provided categories will be sorted by alphabetically according to their identifiers, first by namespace, and then by path. All vanilla categories will remain in their natural order.

#### Debug Text API

It is now possible to register debug HUD entries to be added to the debug (F3) overlay. The debug overlay is now also available outside of a world. Use `DebugHudEntries` to register entries to be rendered as follows:

```
DebugHudEntries.register(
  Identifier.of("test", "example"), 
  new DebugHudEntry() {
    @Override
    public void render(
      DebugHudLines lines, 
      @Nullable World world, 
      @Nullable WorldChunk clientChunk, 
      @Nullable WorldChunk chunk
    ) {
      if (world != null) lines.addLine("Example in-world line :)");
      else lines.addLine("Example out-of-world line :(");
    }

    @Override
    public boolean canShow(boolean reducedDebugInfo) {
      // return false if your debug text 
      // is not applicable with reduced debug info
      return true;
    }
  }
);
```

#### Misc

##### MacOS

`MinecraftClient.IS_SYSTEM_MAC` has been replaced by `SystemKeycodes.IS_MAC_OS`.
