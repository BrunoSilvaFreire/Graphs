plugins {
    kotlin("jvm")
    application
}
dependencies {
    api(rootProject)
    api("guru.nidi:graphviz-kotlin:0.15.1")
    api("com.eclipsesource.j2v8:j2v8_linux_x86_64:4.6.0")
}

application {
    mainClassName = "me.ddevil.ia.MainKt"

}

tasks.distZip {
    val thisMain = project.sourceSets.main.get()
    into("sources/application") {
        from(thisMain.allSource)
    }
    into("sources/graph_library") {
        from(rootProject.sourceSets.main.get().allSource)
    }
}