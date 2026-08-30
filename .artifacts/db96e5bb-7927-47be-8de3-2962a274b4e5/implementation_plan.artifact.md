# Implementation Plan - Fix `braining.android.library` Plugin Not Found

The project is failing to sync because the `ai-providers` module (and potentially others) is trying to use the `braining.android.library` plugin, but Gradle doesn't know where to find it. This plugin is defined as a convention plugin in the `:build-logic:convention` project.

To fix this, we need to register `:build-logic:convention` as an **included build** in `settings.gradle.kts`. This will make the convention plugins available to the rest of the project.

## User Review Required

> [!IMPORTANT]
> This change modifies `settings.gradle.kts` to use `includeBuild("build-logic")`. This is a standard pattern for multi-module Android projects with convention plugins (often called the "Now in Android" pattern).

## Proposed Changes

### Build Configuration

#### [MODIFY] [settings.gradle.kts](file:///C:/Dev/Braining/settings.gradle.kts)

- Add `includeBuild("build-logic")` to the `pluginManagement` block.
- Remove `include(":build-logic:convention")` from the bottom, as it will be managed via `includeBuild`.

## Verification Plan

### Automated Tests
- Run `gradle_sync` to verify that the plugin is now found and the project syncs successfully.
