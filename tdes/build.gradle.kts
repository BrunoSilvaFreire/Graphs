plugins {
    kotlin("jvm")
    application
}
dependencies {
    api(rootProject)
}

application {
    mainClassName = "me.ddevil.graph.GreeterKt"

}
tasks.distZip {
    val thisMain = project.sourceSets.main.get()
    from(thisMain.resources)
    into("sources/application") {
        from(thisMain.allSource)
    }
    into("sources/graph_library") {
        from(rootProject.sourceSets.main.get().allSource)
    }
}