plugins {
    id("java")
    id("kotlin")
    id("maven-publish")

    id("org.spongepowered.gradle.vanilla") version "0.2.1-SNAPSHOT"
    id("at.petra-k.PKSubprojPlugin")
}

val minecraftVersion: String by project
val paucalVersion: String by project
val patchouliVersion: String by project
val inlineVersion: String by project
val jetbrainsAnnotationsVersion: String by project

pkSubproj {
    platform("common")
}

minecraft {
    version(minecraftVersion)
    accessWideners("src/main/resources/hexplat.accesswidener")
}

repositories {
    mavenCentral()

    maven { url = uri("https://maven.blamejared.com") }

    maven {
        // location of the maven that hosts JEI files
        name = "Progwml6 maven"
        url = uri("https://dvs1.progwml6.com/files/maven/")
    }
    maven {
        // location of a maven mirror for JEI files, as a fallback
        name = "ModMaven"
        url = uri("https://modmaven.dev")
    }

    maven { url = uri("https://maven.shedaniel.me/") }

}

dependencies {
    compileOnly(group = "org.spongepowered", name = "mixin", version = "0.8.5")
    implementation(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.1")

    compileOnly("at.petra-k.paucal:paucal-common-$minecraftVersion:$paucalVersion")
    compileOnly("vazkii.patchouli:Patchouli-xplat:$minecraftVersion-$patchouliVersion-SNAPSHOT")

    compileOnly("com.samsthenerd.inline:inline-forge:$minecraftVersion-$inlineVersion")

    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testCompileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.1")
}

tasks {
    test {
        useJUnitPlatform()
    }

    processResources {
        filesMatching("pack.mcmeta") {
            expand(project.properties)
        }
    }
}
