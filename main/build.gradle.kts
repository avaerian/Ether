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

    // General libraries
    implementation("com.google.guava:guava:31.1-jre")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("it.unimi.dsi:fastutil:8.5.6")
    //implementation("org.jooq:joor-java-8:0.9.15")

    // SQL stuffs
    implementation("org.jooq:jooq:3.18.6")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Db drivers
    implementation("org.xerial:sqlite-jdbc:3.43.0.0")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.mysql:mysql-connector-j:8.3.0")

    // Plugin dependencies
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