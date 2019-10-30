plugins {
    kotlin("jvm") version "1.3.50"
}

group = "me.ddevil"
version = "1.0-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
    }
}

dependencies {
    api("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    api("com.google.guava:guava:11.0.2")
    testImplementation(kotlin("test-junit5"))
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}
tasks.test {
    useJUnitPlatform()
}