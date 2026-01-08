import team.idealstate.glass.plugin.project.java.extension.JavaExtension

plugins {
    glass
}

group = "pers.ketikai.minecraft.spigot"
version = "0.1.0-SNAPSHOT"

glass {
    apply<JavaExtension> {
        module("$group.adytitle")

        release(8)

        artifacts {
            manifest {
                main.set("$group.adytitle.Main")
            }
        }

        integration {
            minecraft("1.12.2", "R0.1-SNAPSHOT", SPIGOT)
            lombok()
            junit {
                mockito()
            }
        }

        publication()
    }
}

repositories {
    sonatype(SNAPSHOTS)
    maven {
        name = "placeholderapi"
        url = uri("https://repo.extendedclip.com/releases/")
    }
    maven {
        name = "nustar"
        url = uri("https://maven.nustar.top/repository/nustar-snapshots/")
    }
    maven {
        name = "tabooproject"
        url = uri("https://repo.tabooproject.org/repository/releases/")
    }
}

dependencies {
    compileOnly(libs.minecraft.next.spigot)
    compileOnly(libs.placeholderapi)
    compileOnly(fileTree(File(projectDir, "libraries")))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:1.5.20")
}

publishing {
    repositories {
        local(project)
    }
}

tasks {
    processResources {
        val properties =
            mapOf(
                "version" to project.version,
            )
        filesMatching("plugin.yml") {
            expand(properties)
        }
    }
}
