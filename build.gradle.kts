plugins {
    kotlin("jvm") version "1.3.50"
}

group = "me.ddevil"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("com.google.guava:guava:11.0.2")

}