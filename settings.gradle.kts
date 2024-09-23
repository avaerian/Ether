plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "Ether"

includeBuild("build-logic")

include("core")

// Modules to exclude if incomplete/erroneous
val excludedNmsModules = Array<String>(0) {
    ""
}

logger.lifecycle("NMS modules to exclude: ${excludedNmsModules.contentToString()}")


val nmsDir: Array<File> = file("nms").listFiles() ?: throw GradleException("No NMS module/directory was found!")
for (dir in nmsDir) {
    if(!excludedNmsModules.contains(dir.name)) {
        include("nms:${dir.name}")
        logger.lifecycle("Included nms version: [name=${dir.name}, path=\'${dir.path}\']")
        continue
    }
    logger.lifecycle("NMS module ${dir.name} not included")
}