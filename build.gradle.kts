import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    //`kotlin-dsl`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version("9.3.1")
}

group = rootProject.group
version = rootProject.version

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val shade by configurations.creating {
    extendsFrom(configurations.implementation.get())
}

repositories {
    maven("https://repo.carm.cc/repository/maven-public/")
    gradlePluginPortal()
    mavenCentral()
}

// TODO: create a SourceSet "shade" for shadowJar to clarify shading?

dependencies {
    compileOnly(libs.slf4j)
    compileOnly(libs.log4j)

    shade(project(":core"))
    shade(project(":nms:v1_20_R2", "reobf"))

    // shade in these dependencies as well
    shade(libs.reflectionRemapper)
    shade(libs.jooq)
    shade(libs.hikariCP)
    shade(libs.sql.driver.h2)
    shade(libs.sql.driver.sqlite)
    shade(libs.sql.driver.mysql)
    shade(libs.sql.driver.postgresql)
}

tasks.named<ShadowJar>("shadowJar") {

    //configurations = project.configurations.compileClasspath.map { listOf(it) }
    configurations = listOf(shade)

    exclude("*.properties") // TODO: review
    archiveFileName.set("${project.name}-${project.version}.jar")

    fun reloc(pkg: String) = relocate(pkg, "$group.relocate.$pkg")
    reloc("net.fabricmc.mappingio")
    reloc("org.jooq")
    reloc("com.zaxxer")

    /*minimize {
        exclude(project(":core"))
        exclude(project(":nms:v1_20_R2"))
    }*/

    // TODO: review this
    //reloc("org.postgresql")
    //reloc("com.h2database")
    //reloc("com.mysql")
    //reloc("org.xerial")
}

tasks.withType<JavaCompile> {
    options.release.set(17)
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}