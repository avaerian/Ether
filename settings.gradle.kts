plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "Ether"

includeBuild("build-logic")

include("core")

// Include NMS modules
val nmsExpected = true
logger.lifecycle("NMS expected? $nmsExpected")

if(nmsExpected) {
    val nmsDir = file("nms").listFiles() ?: throw GradleException("No NMS module/directory was found!")
    nmsDir.forEach {
        include("nms:${it.name}")
        logger.lifecycle("Included nms version: [name=${it.name}, path=\'${it.path}\']")
    }
}

