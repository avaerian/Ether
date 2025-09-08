plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://repo.carm.cc/repository/maven-public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    gradlePluginPortal()
    mavenCentral()
}

dependencies {

    implementation(libs.paperweight)
    implementation(libs.shadow)

    /*
    compileOnly(libs.paperApi)

    /*project.project(":nms").subprojects.forEach {
        implementation(project(it.path))
    }*/

    constraints {
        val asmVersion = "[${libs.versions.minAsm.get()},)"
        implementation("org.ow2.asm:asm:$asmVersion") {
            because("Java 21 support")
        }
        implementation("org.ow2.asm:asm-commons:$asmVersion") {
            because("Java 21 support")
        }
    }
    */

}

