pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven {
            name = "Fabric"
            url = uri("https://maven.fabricmc.net/")
        }
        maven {
            name = "Sponge Snapshots"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        maven { url = uri("https://maven.blamejared.com") }
        maven { url = uri("https://maven.minecraftforge.net") }
    }

    plugins {
        val kotlinVersion: String by settings
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
    }
}

rootProject.name = "Hex Casting"
include("Common", "Fabric", "Forge")
