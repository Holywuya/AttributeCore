import io.izzel.taboolib.gradle.*
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    java
    id("io.izzel.taboolib") version "2.0.27"
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
}

taboolib {
    env {
        install(Basic)
        install(BukkitUI)
        install(BukkitHook)
        install(Bukkit)
        install(BukkitUtil)
        install(DatabasePlayer)
        install(BukkitNMSItemTag)
        install(Kether)
        install(JavaScript)
    }
    description {
        name = "AttributeCore"
        contributors {
            name("Esters")
        }
    }
    relocate("ink.ptms.um", "com.attributecore.um")
    version { taboolib = "6.2.4-e6c8347" }
}

repositories {
    maven("https://nexus.maplex.top/repository/maven-public/")
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

 dependencies {
     compileOnly("ink.ptms.core:v12004:12004:mapped")
     compileOnly("ink.ptms.core:v12004:12004:universal")
     taboo("ink.ptms:um:1.2.1")
     compileOnly(kotlin("stdlib"))
     compileOnly(fileTree("libs"))
     taboo("org.openjdk.nashorn:nashorn-core:15.4")
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