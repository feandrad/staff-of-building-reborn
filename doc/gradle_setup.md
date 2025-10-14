# Gradle Setup for New Projects

This document provides instructions on how to set up Gradle files and properties for new projects based on the existing structure.

## `settings.gradle`

The `settings.gradle` file is responsible for defining the project structure and including sub-projects (modules).

### Example

```groovy
pluginManagement {
    repositories {
        maven { url "https://maven.fabricmc.net/" }
        maven { url "https://maven.architectury.dev/" }
        maven { url "https://files.minecraftforge.net/maven/" }
        gradlePluginPortal()
    }
    plugins {
        id 'org.jetbrains.kotlin.jvm' version '2.2.0'
    }
}
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'your-project-name'

include 'common'
include 'fabric'
include 'neoforge'
```

### Instructions

1.  **`rootProject.name`**: Change `'metalmancy'` to your project's name.
2.  **`include`**: Add or remove modules as needed for your project structure. The example includes `common`, `fabric`, and `neoforge`.

## `gradle.properties`

The `gradle.properties` file contains project-wide properties, such as versions and configuration options.

### Example

```properties
# Mod properties
mod_version=1.0.0
maven_group=io.yourname
archives_name=your-mod-id
enabled_platforms=fabric,neoforge

# Minecraft properties
minecraft_version=1.21.10

# Dependencies
architectury_api_version=18.0.3
fabric_loader_version=0.17.2
fabric_api_version=0.134.1+1.21.10
neoforge_version=21.10.0-beta

# Gradle JVM arguments
org.gradle.java.home=/path/to/your/java/home
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
```

### Instructions

1.  **Mod Properties**:
    *   `mod_version`: Set the initial version of your mod.
    *   `maven_group`: Change `io.felipeandrade` to your own group ID.
    *   `archives_name`: Set a unique name for your mod artifacts.
    *   `enabled_platforms`: List the platforms you intend to support (e.g., `fabric`, `neoforge`).
2.  **Minecraft and Dependency Versions**:
    *   Update `minecraft_version` and other dependency versions as needed.
3.  **Gradle Configuration**:
    *   `org.gradle.java.home`: Ensure this points to a valid JDK installation.
    *   Adjust `org.gradle.jvmargs` if you need to allocate more or less memory to Gradle.

## `build.gradle`

The root `build.gradle` file configures the build process for the entire project. It applies plugins and sets configurations for the sub-projects. Review this file to understand how the build is orchestrated and make adjustments as necessary for your project's needs.
