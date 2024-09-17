plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
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

