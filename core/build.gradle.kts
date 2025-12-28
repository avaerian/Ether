import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
    id("me.champeau.jmh") version("0.7.2")
}

//sourceSets["jmh"].compileClasspath += sourceSets["main"].runtimeClasspath
//sourceSets["jmh"].runtimeClasspath += sourceSets["main"].runtimeClasspath

repositories {
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.carm.cc/repository/maven-public/")
    mavenCentral()
    mavenLocal()
    //maven("https://libraries.minecraft.net")
}

dependencies {

    //implementation("org.jooq:joor-java-8:0.9.15")
    compileOnly(libs.paperApi)

    implementation(libs.dataFixerUpper) // TODO: review; move to :build-logic build.gradle.kts for version constraint management; compileOnly / compile ??
    // should also review if this needs to be shaded

    // TODO: move these to :build-logic build.gradle.kts with version constraints for better Mojang lib conflict handling
    // General libraries
    implementation(libs.guava)
    implementation(libs.gson)
    implementation(libs.fastutil)
    implementation(libs.netty.buffer)

    // SQL libraries
    implementation(libs.jooq)
    implementation(libs.hikariCP)

    implementation(libs.sql.driver.sqlite)
    implementation(libs.sql.driver.postgresql)
    implementation(libs.sql.driver.h2)
    implementation(libs.sql.driver.mysql)

    compileOnly(libs.worldeditBukkit)

    // Benchmarking
    jmhImplementation(libs.jmh.core)
    jmhAnnotationProcessor(libs.jmh.annprocessor)

    // Unit tests
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.named<ShadowJar>("shadowJar") {

    // TODO: use shadow configuration?
    configurations = listOf() // shade none of the dependencies

    // debug
    logger.lifecycle("Dependencies:")
    project.configurations.runtimeClasspath.get().resolvedConfiguration.firstLevelModuleDependencies.forEach {
        logger.lifecycle(it.name)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}