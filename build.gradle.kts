import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    //`kotlin-dsl`
    `java-library`
    id("java")
    id("com.gradleup.shadow") version("8.3.1")
}

group = rootProject.group
version = rootProject.version

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

// TODO: create a SourceSet "shade" for shadowJar to clarify shading?

dependencies {
    implementation(project(":core"))
    implementation(project(":nms:v1_20_R2", "reobf"))

    // shade in these dependencies as well
    implementation(libs.reflectionRemapper)
}

tasks.named<ShadowJar>("shadowJar") {

    configurations = listOf(project.configurations.runtimeClasspath.get())

    exclude("*.properties") // TODO: review
    archiveFileName.set("${project.name}-${project.version}.jar")

    fun reloc(pkg: String) = relocate(pkg, "$group.relocate.$pkg")
    reloc("net.fabricmc.mappingio")
}

tasks.withType<JavaCompile> {
    options.release.set(17)
    options.encoding = Charsets.UTF_8.name()
}

tasks.withType<Javadoc> {
    options.encoding = Charsets.UTF_8.name()
}