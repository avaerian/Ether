import buildlogic.getLibrary
import buildlogic.getVersion
import buildlogic.libsCatalog

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev")
}

// TODO: for properly declaring: https://docs.gradle.org/current/userguide/declaring_repositories.html
repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.carm.cc/repository/maven-public/")
    gradlePluginPortal()
    mavenCentral()
    mavenLocal()
}

configurations.all {
    resolutionStrategy.force("net.fabricmc:tiny-remapper:${libsCatalog.getVersion("minTinyRemapper")}")
}

/*
tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        exclude("org.ow2.asm:asm")
        //exclude("org.ow2.asm:asm-commons")
    }
}
*/

dependencies {
    compileOnly(project(":core"))
    implementation(libsCatalog.getLibrary("reflectionRemapper"))

    constraints {
        val asmVersion = "[${libsCatalog.getVersion("minAsm")},)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Java 21 support")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Java 21 support")
        }

        remapper("net.fabricmc:tiny-remapper:[${libsCatalog.getVersion("minTinyRemapper")},)") {
            because("Java 21 support")
        }
    }
}

/*tasks.named("assemble") {
    dependsOn("reobfJar")
}*/

