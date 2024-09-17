import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `kotlin-dsl`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version("8.3.1")
    //id("io.papermc.paperweight.userdev") version("1.7.2") apply(false)
}

group = rootProject.group
version = rootProject.version

dependencies {
    implementation(project(":core"))
    implementation(project(":nms:v1_19_R3", "reobf"))

    implementation(libs.reflectionRemapper)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    configurations = listOf(project.configurations.runtimeClasspath.get()) // TODO: review
    //configurations = listOf(project.configurations["compile"])

    //exclude("module-info.class")
    exclude("*.properties")

    dependencies {
        include(project(":core"))
        include(project(":nms:v1_19_R3"))
        include(dependency("xyz.jpenilla:reflection-remapper"))
    }
}

allprojects {
    apply(plugin = "java")
    //apply(plugin = "com.github.johnrengelman.shadow")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
        //sourceCompatibility = JavaVersion.VERSION_21
    }

    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    tasks.withType<JavaCompile> {
        options.release.set(21)
        options.encoding = Charsets.UTF_8.name()
    }

    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }

    /*tasks.withType<ShadowJar> {
        archiveClassifier.set("") // SUPER IMPORTANT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        dependencies {
            include(project(":core"))
            include(project(":nms:v1_19_R3"))
            include(dependency("xyz.jpenilla:reflection-remapper"))
        }
    }*/
}


/*
compileJava.options.encoding = 'UTF-8'

tasks.withType<Test> {
    //systemProperties = System.getProperties()
    systemProperties.remove("java.endorsed.dirs")
}

tasks.create("runBinaryTests", Test::class) {
    dependsOn("shadowJar")
    val FAT_JAR_FILEPATH = "$projectDir/build/libs/${project.name}-$version-all.jar"
    testClassesDirs += zipTree(FAT_JAR_FILEPATH)
    classpath = project.files(FAT_JAR_FILEPATH, configurations.runtimeClasspath)
    outputs.upToDateWhen { false }
}

task runBinaryTests(type: Test) {
    testClassesDirs += zipTree($projectDir/fatjar.jar)
    classpath = project.files( "$projectDir/fatjar.jar", configurations.runtime )
    outputs.upToDateWhen { false }
}
*/