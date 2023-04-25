plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2" apply(false)
    id("java")

    // TODO: create a global constant for this plugin id
    id("io.papermc.paperweight.userdev") version("1.5.4") apply(false)
}

dependencies {

    implementation(project(":v1_19_R2", "reobf"))

    implementation(project(":main"))
}

allprojects {

    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    group = "org.minerift.ether"
    version = "1.0-SNAPSHOT"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }
}

// Paper-API dependency for submodules
subprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    }

    dependencies {
        compileOnly("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPSHOT")
    }
}

// Once more versions are implemented, this list will grow
configure(subprojects.filter { listOf("v1_19_R2").contains(it.name) }) {
    apply(plugin = "io.papermc.paperweight.userdev")
}