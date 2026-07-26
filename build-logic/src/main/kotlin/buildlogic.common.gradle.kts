import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.gradleup.shadow")
}

val shade: Configuration by configurations.creating {
    //extendsFrom(configurations.runtimeClasspath.get())
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

    reloc("org.postgresql")
    reloc("com.h2database")
    reloc("com.mysql")
    reloc("org.xerial")
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
