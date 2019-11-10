plugins {
    kotlin("jvm")
}
val gdxVersion: String by extra
dependencies {
    api(rootProject)
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
}
sourceSets.main {
    java.srcDir("src")
}