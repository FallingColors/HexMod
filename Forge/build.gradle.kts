plugins {
    id("java")
    id("kotlin")
    id("maven-publish")

    id("at.petra-k.PKSubprojPlugin")

    id("net.minecraftforge.gradle") version "6.0.+"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
}

val modID: String by project
val minecraftVersion: String by project
val forgeVersion: String by project
val kotlinForForgeVersion: String by project
val paucalVersion: String by project
val patchouliVersion: String by project
val caelusVersion: String by project
val inlineVersion: String by project
val clothConfigVersion: String by project
val jeiVersion: String by project
val curiosVersion: String by project
val pehkuiVersion: String by project
@Suppress("PropertyName")
val forge_ats_enabled: String? by project

pkSubproj {
    platform("forge")
    curseforgeJar(tasks.jar.get().archiveFile)
    curseforgeDependencies(listOf())
    modrinthJar(tasks.jar.get().archiveFile)
    modrinthDependencies(listOf())
}

repositories {
    mavenCentral()

    // If you have mod jar dependencies in ./libs, you can declare them as a repository like so:
    flatDir {
        dir("libs")
    }

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
    // caelus elytra
    maven { url = uri("https://maven.theillusivec4.top") }
    // pehkui
    maven { url = uri("https://jitpack.io") }

    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }

    maven { url = uri("https://maven.shedaniel.me/") }
}

dependencies {
    minecraft("net.minecraftforge:forge:${minecraftVersion}-${forgeVersion}")
    compileOnly(project(":Common"))

    implementation("thedarkcolour:kotlinforforge:$kotlinForForgeVersion")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    compileOnly(fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion"))
    runtimeOnly(fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion"))
    compileOnly(fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FORGE-SNAPSHOT"))
    runtimeOnly(fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FORGE-SNAPSHOT"))

    // aughh
    testCompileOnly(fg.deobf("at.petra-k.paucal:paucal-forge-$minecraftVersion:$paucalVersion"))
    testCompileOnly(fg.deobf("vazkii.patchouli:Patchouli:$minecraftVersion-$patchouliVersion-FORGE-SNAPSHOT"))

    implementation(fg.deobf("top.theillusivec4.caelus:caelus-forge:$caelusVersion"))

    implementation(fg.deobf("com.samsthenerd.inline:inline-forge:$minecraftVersion-$inlineVersion"))

    // needed for inline to run
    runtimeOnly(fg.deobf("me.shedaniel.cloth:cloth-config-forge:$clothConfigVersion"))

    // Optional interop

    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-common-api:$jeiVersion"))
    compileOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge-api:$jeiVersion"))
    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))

    compileOnly(fg.deobf("top.theillusivec4.curios:curios-forge:$curiosVersion+$minecraftVersion:api"))
    runtimeOnly(fg.deobf("top.theillusivec4.curios:curios-forge:$curiosVersion+$minecraftVersion"))

    api(fg.deobf("com.github.Virtuoel:Pehkui:${pehkuiVersion}-$minecraftVersion-forge"))
}

minecraft {
    mappings("official", minecraftVersion)
    accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))

    if (forge_ats_enabled.toBoolean()) {
        // This location is hardcoded in Forge and can not be changed.
        // https://github.com/MinecraftForge/MinecraftForge/blob/be1698bb1554f9c8fa2f58e32b9ab70bc4385e60/fmlloader/src/main/java/net/minecraftforge/fml/loading/moddiscovery/ModFile.java#L123
        accessTransformer(file("src/main/resources/META-INF/accesstransformer.cfg"))
        project.logger.debug("Forge Access Transformers are enabled for this project.")
    }

    runs {
        register("client") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create(modID) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }

        register("server") {
            workingDirectory(project.file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create(modID) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }

        // We have to have a dummy data run to be parented from
        register("data") {}

        register("xplatDatagen") {
            parent(minecraft.runs.get("data"))

            workingDirectory(file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            args("--mod", modID, "--all", "--output", file("../Common/src/generated/resources/"), "--existing", file("../Common/src/main/resources/"))
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            property("hexcasting.xplat_datagen", "true")

            mods {
                create(modID) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }

        register("forgeDatagen") {
            parent(minecraft.runs.get("data"))

            workingDirectory(file("run"))
            ideaModule("${rootProject.name}.${project.name}.main")
            args("--mod", modID, "--all", "--output", file("src/generated/resources/"), "--existing", file("src/main/resources/"))
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            property("hexcasting.forge_datagen", "true")
            mods {
                create(modID) {
                    source(sourceSets.main.get())
                    source(project(":Common").sourceSets.main.get())
                }
            }
        }
    }
}


mixin {
    add(sourceSets.main.get(), "hexcasting.mixins.refmap.json")
    config("hexplat.mixins.json")
    config("hexcasting_forge.mixins.json")
}

sourceSets {
    main {
        resources {
            srcDir(file("src/generated/resources"))
            srcDir(project(":Common").file("src/generated/resources"))
        }

        kotlin {
            srcDir("src/main/java")
        }
    }

    test {
        kotlin {
            srcDir("src/main/java")
        }
    }
}

tasks {
    compileJava {
        source(project(":Common").sourceSets.main.get().allSource)
    }

    compileKotlin {
        source(project(":Common").sourceSets.main.get().kotlin)
    }

    compileTestKotlin {
        source(project(":Common").sourceSets.main.get().kotlin)
    }

    processResources {
        from(project(":Common").sourceSets.main.get().resources)
        inputs.property("version", version)

        filesMatching("mods.toml") {
            expand("version" to version)
        }
    }

    jar {
        finalizedBy("reobfJar")
    }
}
