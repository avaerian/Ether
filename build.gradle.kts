plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2" apply(false)
    id("java")

    // TODO: create a global constant for this plugin id
    id("io.papermc.paperweight.userdev") version("1.5.4") apply(false)
}

dependencies {

    implementation(project(":v1_19_R2"))

    implementation(project(":main"))
}

/*shadowJar {
    //configurations = [project.configurations.compileClasspath]
    dependencies {
        exclude(dependency("com.google.guava:guava:31.1-jre"))
        //exclude(dependency("io.papermc.paper:paper-api:1.19.3-R0.1-SNAPCHAT"))
    }
}*/

allprojects {

    apply(plugin = "java")
    apply(plugin = "com.github.johnrengelman.shadow")

    //group("org.minerift")
    //version("1.0-SNAPSHOT")
    group = "org.minerift.ether"
    version = "1.0-SNAPCHAT"

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories {
        mavenCentral()
    }

    /*shadowJar {
        configurations = [project.configurations.runtimeClasspath]
    }*/
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


/*configure(subprojects.findAll { ["v1_19_R2"].contains(it.name) }) {
    //apply plugin: "io.papermc.paperweight.userdev"
    apply(plugin = "io.papermc.paperweight.userdev")
}*/

