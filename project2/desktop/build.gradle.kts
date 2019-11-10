plugins {
    kotlin("jvm")
    application
}
val gdxVersion: String by extra

dependencies {
    api(project(":project2:core"))
    api("com.badlogicgames.gdx:gdx-backend-lwjgl:$gdxVersion")
    api("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")

}

sourceSets.main {
    java.srcDir("src/")
    resources.srcDir("../core/assets")
}
application {
    mainClassName = "me.ddevil.project2.desktop.DesktopLauncher"
}