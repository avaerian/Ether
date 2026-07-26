import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
    `java-library`
    id("me.champeau.jmh") version("0.7.2")
}

//sourceSets["jmh"].compileClasspath += sourceSets["main"].runtimeClasspath
//sourceSets["jmh"].runtimeClasspath += sourceSets["main"].runtimeClasspath

repositories {
    mavenCentral()
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.carm.cc/repository/maven-public/")
    mavenLocal()
}

val shade: Configuration = configurations.maybeCreate("shade")
    //.extendsFrom(configurations.runtimeClasspath.get())


dependencies {
    api(libs.slf4j)
    api(libs.log4j) {
        exclude(group = "jakarta.platform", module = "jakartaee-api-parent")
    }

    //implementation("org.jooq:joor-java-8:0.9.15")
    compileOnly(libs.paperApi)

    compileOnly(libs.dataFixerUpper) // TODO: review; move to :build-logic build.gradle.kts for version constraint management; compileOnly / compile ??
    // should also review if this needs to be shaded

    // TODO: move these to :build-logic build.gradle.kts with version constraints for better Mojang lib conflict handling
    // TODO: review compileOnly
    // General libraries
    compileOnly(libs.guava)
    compileOnly(libs.gson)
    compileOnly(libs.fastutil)
    compileOnly(libs.netty.buffer)

    // SQL libraries
    implementation(libs.jooq)
    implementation(libs.hikariCP)

    shade(libs.sql.driver.sqlite)
    shade(libs.sql.driver.postgresql)
    shade(libs.sql.driver.h2)
    shade(libs.sql.driver.mysql)

    compileOnly(libs.worldeditBukkit)

    // Benchmarking
    jmhImplementation(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annprocessor)

    // Unit tests
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)

    // Lombok
    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)
}

tasks.named<ShadowJar>("shadowJar") {

    configurations = listOf(shade)

    logger.lifecycle("\nDependencies:")
    shade.dependencies.forEach {
        logger.lifecycle(it.name)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}