plugins {
    id("com.gradleup.shadow")
}

/*tasks.named<ShadowJar>("shadowJar") {
    archiveClassifier.set("")
    configurations = [project.configurations.compileClasspath]

    exclude("module-info.class")
    exclude("*.properties")

    dependencies {
        include(project(":core"))
        include(project(":nms:v1_19_R3"))
        include(dependency("xyz.jpenilla:reflection-remapper"))
    }
}*/