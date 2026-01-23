import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import io.izzel.taboolib.gradle.Basic
import io.izzel.taboolib.gradle.BukkitHook
import io.izzel.taboolib.gradle.BukkitNMS
import io.izzel.taboolib.gradle.BukkitUI
import io.izzel.taboolib.gradle.JavaScript
import io.izzel.taboolib.gradle.Kether
import io.izzel.taboolib.gradle.Bukkit
import io.izzel.taboolib.gradle.MinecraftChat
import io.izzel.taboolib.gradle.DatabasePlayer
import io.izzel.taboolib.gradle.Database
import io.izzel.taboolib.gradle.CommandHelper
import io.izzel.taboolib.gradle.BukkitNMSDataSerializer
import io.izzel.taboolib.gradle.BukkitNMSEntityAI
import io.izzel.taboolib.gradle.BukkitNMSItemTag
import io.izzel.taboolib.gradle.BukkitNMSUtil


plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

taboolib {
    env {
        install(Basic)
        install(BukkitHook)
        install(BukkitNMS)
        install(BukkitUI)
        install(Kether)
        install(Bukkit)
        install(MinecraftChat)
        install(DatabasePlayer)
        install(Database)
        install(CommandHelper)
        install(BukkitNMSDataSerializer)
        install(BukkitNMSEntityAI)
        install(BukkitNMSItemTag)
        install(BukkitNMSUtil)
    }
    description {
        name = "AttributeCore"
        contributors {
            name("Esters")
        }
    }
    version { taboolib = "6.2.4-e6c8347" }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v12004:12004:universal")
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JVM_1_8)
        freeCompilerArgs.add("-Xjvm-default=all")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}