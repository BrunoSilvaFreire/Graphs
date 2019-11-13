plugins {
    kotlin("jvm")
}
val gdxVersion: String by extra
dependencies {
    api(rootProject)
    api("com.badlogicgames.gdx:gdx:$gdxVersion")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
}
sourceSets.main {
    java.srcDir("src")
}