plugins {
    id("me.champeau.jmh") version("0.7.0")
}

sourceSets["jmh"].compileClasspath += sourceSets["main"].runtimeClasspath
sourceSets["jmh"].runtimeClasspath += sourceSets["main"].runtimeClasspath

repositories {
    mavenCentral()
    maven { url = uri("https://maven.enginehub.org/repo/") }
}

dependencies {

    implementation("com.google.guava:guava:31.1-jre")
    implementation("it.unimi.dsi:fastutil:8.5.6")

    compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.14")

    jmhImplementation("org.openjdk.jmh:jmh-core:1.36")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    //testImplementation(project(":v1_19_R2"))

}

tasks.withType<Test> {
    useJUnitPlatform()
}