plugins {
    kotlin("jvm")

}
repositories {
    mavenCentral()
}

dependencies {
    api(rootProject)
    compile("org.apache.commons:commons-email:1.5")
}