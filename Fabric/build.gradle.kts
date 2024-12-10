plugins {
    id("java")
    id("kotlin")
    id("maven-publish")

    id("fabric-loom") version "1.6-SNAPSHOT"
    id("at.petra-k.PKSubprojPlugin")
}

val modID: String by project
val minecraftVersion: String by project
val fabricLanguageKotlinVersion: String by project
val fabricLoaderVersion: String by project
val fabricVersion: String by project
val jetbrainsAnnotationsVersion: String by project
val paucalVersion: String by project
val patchouliVersion: String by project
val inlineVersion: String by project
val cardinalComponentsVersion: String by project
val serializationHooksVersion: String by project
val clothConfigVersion: String by project
val trinketsVersion: String by project
val emiVersion: String by project
val pehkuiVersion: String by project
val modmenuVersion: String by project
val lithiumVersion: String by project

pkSubproj {
    platform("fabric")
    curseforgeJar(tasks.remapJar.get().archiveFile)
    curseforgeDependencies(listOf())
    modrinthJar(tasks.remapJar.get().archiveFile)
    modrinthDependencies(listOf())
}

loom {
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName = "hexcasting.mixins.refmap.json"

    accessWidenerPath = file("src/main/resources/fabricasting.accesswidener")

    runs {
        named("client") {
            client()
            configName = "Fabric Client"
        }
        named("server") {
            server()
            configName = "Fabric Server"
        }
        register("datagen") {
            client()
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.modid=${modID}")
            vmArg("-Dfabric-api.datagen.output-dir=${file("src/generated/resources")}")
        }

        configureEach {
            runDir("Fabric/run")
            ideConfigGenerated(true)
        }
    }
}

repositories {
    mavenCentral()

    // paucal and patchi
    maven { url = uri("https://maven.blamejared.com") }
    // modmenu and clothconfig
    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.ladysnake.org/releases") }
    // Entity reach
    maven { url = uri("https://maven.jamieswhiteshirt.com/libs-release/") }
    maven { url = uri("https://mvn.devos.one/snapshots/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
    exclusiveContent {
        forRepository {
            maven { url = uri("https://api.modrinth.com/maven") }
        }
        filter { includeGroup("maven.modrinth") }
    }
    // pehkui
    maven { url = uri("https://jitpack.io") }

    // If you have mod jar dependencies in ./libs, you can declare them as a repository like so:
    flatDir {
        dir("libs")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-language-kotlin:${fabricLanguageKotlinVersion}")
    modImplementation("net.fabricmc:fabric-loader:${fabricLoaderVersion}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabricVersion}")

    // Reqs
    compileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    testCompileOnly("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    compileOnly("com.demonwav.mcdev:annotations:1.0")

    implementation(group = "com.google.code.findbugs", name = "jsr305", version = "3.0.1")
    compileOnly(project(":Common"))

    modImplementation("at.petra-k.paucal:paucal-fabric-$minecraftVersion:$paucalVersion")
    modImplementation("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FABRIC-SNAPSHOT")

    modImplementation("com.samsthenerd.inline:inline-fabric:$minecraftVersion-$inlineVersion")

    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-base:$cardinalComponentsVersion")
//    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-util:$cardinalComponentsVersion")
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-entity:$cardinalComponentsVersion")
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-item:$cardinalComponentsVersion")
    modImplementation("dev.onyxstudios.cardinal-components-api:cardinal-components-block:$cardinalComponentsVersion")

//    modImplementation("com.jamieswhiteshirt:reach-entity-attributes:${entityReachVersion}")
//    include("com.jamieswhiteshirt:reach-entity-attributes:${entityReachVersion}")

    // apparently the 1.18 version Just Works on 1.19
    modImplementation("${modID}:serialization-hooks:$serializationHooksVersion")
    include("${modID}:serialization-hooks:$serializationHooksVersion")
    "com.github.LlamaLad7:MixinExtras:0.1.1".also {
        implementation(it)
        include(it)
    }

    // Optional integrations

    modApi("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    modImplementation("dev.emi:trinkets:$trinketsVersion")
    modImplementation("dev.emi:emi-fabric:${emiVersion}")

//    modImplementation("maven.modrinth:gravity-api:$gravityApiVersion")
    modApi("com.github.Virtuoel:Pehkui:${pehkuiVersion}") {
        exclude(group = "net.fabricmc.fabric-api")
    }

    // *Something* is including an old version of modmenu with a broken mixin
    // We can't figure out what it is
    // so i'm just including a fresher version
    modImplementation("com.terraformersmc:modmenu:$modmenuVersion")

    // i am speed
    // sodium is causing frustum mixin errors so don't use it
//    modImplementation("maven.modrinth:sodium:${sodiumVersion}")
    modImplementation("maven.modrinth:lithium:${lithiumVersion}")
//    modImplementation("maven.modrinth:phosphor:${phosphorVersion}")
}

sourceSets {
    main {
        resources {
            srcDir(file("src/generated/resources"))
            srcDir(project(":Common").file("src/generated/resources"))
        }
    }
}

tasks {
    withType<JavaCompile> {
        source(project(":Common").sourceSets.main.get().allSource)
    }

    compileKotlin {
        source(project(":Common").sourceSets.main.get().kotlin)
    }

    sourcesJar {
        from(project(":Common").sourceSets.main.get().allJava)
    }

    processResources {
        from(project(":Common").sourceSets.main.get().resources)
        inputs.property("version", version)

        filesMatching("fabric.mod.json") {
            expand("version" to version)
        }
    }
}
