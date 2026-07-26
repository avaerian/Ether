import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.named

plugins {
    //`kotlin-dsl`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version("9.3.1")
    id("buildlogic.common")
}

group = rootProject.group
version = rootProject.version

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// TODO: move this to build-logic

repositories {
    mavenCentral()
    maven("https://repo.carm.cc/repository/maven-public/")
    gradlePluginPortal()
}

// TODO: create a SourceSet "shade" for shadowJar to clarify shading?
val shade: Configuration = configurations.maybeCreate("shade")
    //.extendsFrom(configurations.runtimeClasspath.get())

dependencies {
    api(libs.slf4j)
    api(libs.log4j) {
        exclude(group = "jakarta.platform", module = "jakartaee-api-parent")
    }

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

    compileOnly(libs.lombok) // FIXME: compileOnly
    annotationProcessor(libs.lombok)

    //testCompileOnly(libs.lombok)
    //testAnnotationProcessor(libs.lombok)
}

tasks.named<ShadowJar>("shadowJar") {

    configurations = listOf(shade)

    logger.lifecycle("cfg dependencies:")
    configurations.get().any { it == shade }.run {
        val cfg = configurations.get().elementAt(0);
        logger.lifecycle("dependencies:")
        cfg.dependencies.forEach { logger.lifecycle(it.name) }

        logger.lifecycle("all dependencies:")
        cfg.allDependencies.forEach { logger.lifecycle(it.name) }
    }

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