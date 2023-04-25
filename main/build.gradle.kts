plugins {
    id("me.champeau.jmh") version("0.7.0")
}

sourceSets["jmh"].compileClasspath += sourceSets["main"].runtimeClasspath
sourceSets["jmh"].runtimeClasspath += sourceSets["main"].runtimeClasspath

dependencies {

    compileOnly("com.google.guava:guava:31.1-jre")
    shadow("com.google.guava:guava:31.1-jre")

    jmhImplementation("org.openjdk.jmh:jmh-core:1.36")
    jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

}

tasks.withType<Test> {
    useJUnitPlatform()
}